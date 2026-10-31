#!/bin/bash
set -e

echo "=========================================="
echo " 1/5: Building Frontend with Docker"
echo "=========================================="
docker build --target build -t frontend-build src/frontend
docker rm -f temp-fe 2>/dev/null || true
docker create --name temp-fe frontend-build
rm -rf src/frontend/build
docker cp temp-fe:/app/build ./src/frontend/
docker rm temp-fe

echo "=========================================="
echo " 2/5: Uploading Frontend to AWS S3"
echo "=========================================="
aws s3 sync src/frontend/build s3://three-tier-fe-s3-280769660855 --delete

echo "=========================================="
echo " 3/5: Building Backend Container"
echo "=========================================="
docker build --target prod -t 280769660855.dkr.ecr.ap-south-1.amazonaws.com/three-tier-backend:latest src/backend

echo "=========================================="
echo " 4/5: Pushing Backend to AWS ECR"
echo "=========================================="
aws ecr describe-repositories --repository-names three-tier-backend --region ap-south-1 2>/dev/null || \
aws ecr create-repository --repository-name three-tier-backend --region ap-south-1

aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin 280769660855.dkr.ecr.ap-south-1.amazonaws.com
docker push 280769660855.dkr.ecr.ap-south-1.amazonaws.com/three-tier-backend:latest

echo "=========================================="
echo " 5/5: Updating AWS ECS Backend Service"
echo "=========================================="
TASK_DEF_FAMILY=$(aws ecs list-task-definitions --region ap-south-1 --query "taskDefinitionArns[-1]" --output text | awk -F'/' '{print $NF}' | awk -F':' '{print $1}')
echo "Found Task Definition: $TASK_DEF_FAMILY"

aws ecs describe-task-definition --task-definition "$TASK_DEF_FAMILY" --region ap-south-1 --query "taskDefinition" > task-def.json

python3 -c "
import json
with open('task-def.json', 'r') as f:
    td = json.load(f)
for k in ['taskDefinitionArn', 'revision', 'status', 'requiresAttributes', 'compatibilities', 'registeredAt', 'registeredBy']:
    td.pop(k, None)
for c in td.get('containerDefinitions', []):
    if c.get('name') == 'backend':
        c['image'] = '280769660855.dkr.ecr.ap-south-1.amazonaws.com/three-tier-backend:latest'
with open('task-def-new.json', 'w') as f:
    json.dump(td, f)
"

NEW_TASK_ARN=$(aws ecs register-task-definition --cli-input-json file://task-def-new.json --region ap-south-1 --query "taskDefinition.taskDefinitionArn" --output text)
echo "New Task Definition registered: $NEW_TASK_ARN"

SERVICE_NAME=$(aws ecs list-services --cluster three-tier-cluster --region ap-south-1 --query "serviceArns[0]" --output text | awk -F'/' '{print $NF}')
echo "Updating ECS Service: $SERVICE_NAME in three-tier-cluster"

aws ecs update-service --cluster three-tier-cluster --service "$SERVICE_NAME" --task-definition "$NEW_TASK_ARN" --force-new-deployment --region ap-south-1

rm -f task-def.json task-def-new.json

echo "=========================================="
echo " DEPLOYMENT SUCCESSFUL!"
echo " Please wait ~2 minutes for ECS container to restart,"
echo " then test Edit & Delete on your website!"
echo "=========================================="

