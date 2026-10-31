#!/bin/bash
set -e
export AWS_PAGER=""

echo "=== Fixing Docker Credential Store ==="
python3 -c "
import json, os
p = os.path.expanduser('~/.docker/config.json')
if os.path.exists(p):
    try:
        with open(p, 'r') as f:
            d = json.load(f)
        d.pop('credsStore', None)
        with open(p, 'w') as f:
            json.dump(d, f)
        print('Fixed ~/.docker/config.json')
    except Exception as e:
        print(e)
"

echo "=========================================="
echo " 1/4: Building Backend Container with Security Fix"
echo "=========================================="
docker build --target prod -t 280769660855.dkr.ecr.ap-south-1.amazonaws.com/three-tier-backend:latest src/backend

echo "=========================================="
echo " 2/4: Pushing Backend Image to AWS ECR"
echo "=========================================="
aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin 280769660855.dkr.ecr.ap-south-1.amazonaws.com
docker push 280769660855.dkr.ecr.ap-south-1.amazonaws.com/three-tier-backend:latest

echo "=========================================="
echo " 3/4: Updating AWS ECS Task & Service"
echo "=========================================="
TASK_DEF_FAMILY=$(aws ecs list-task-definitions --region ap-south-1 --query "taskDefinitionArns[-1]" --output text | awk -F'/' '{print $NF}' | awk -F':' '{print $1}')

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
echo "Registered: $NEW_TASK_ARN"

SERVICE_NAME=$(aws ecs list-services --cluster three-tier-cluster --region ap-south-1 --query "serviceArns[0]" --output text | awk -F'/' '{print $NF}')

aws ecs update-service --cluster three-tier-cluster --service "$SERVICE_NAME" --task-definition "$NEW_TASK_ARN" --force-new-deployment --region ap-south-1

rm -f task-def.json task-def-new.json

echo "=========================================="
echo " 4/4: Waiting for Container to be Healthy (~60s)..."
echo "=========================================="
aws ecs wait services-stable --cluster three-tier-cluster --services "$SERVICE_NAME" --region ap-south-1

echo "=========================================="
echo " SUCCESS! Backend is 100% LIVE and HEALTHY!"
echo " You can now Edit and Delete offers!"
echo "=========================================="
