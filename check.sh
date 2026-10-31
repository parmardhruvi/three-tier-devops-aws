#!/bin/bash
echo "=== 1. ECS Deployments ==="
aws ecs describe-services --cluster three-tier-cluster --services backend --region ap-south-1 --query "services[0].deployments[*].{TaskDef:taskDefinition,Status:status,Desired:desiredCount,Running:runningCount,Pending:pendingCount,Rollout:rolloutState}" --output table

echo ""
echo "=== 2. Recent Tasks ==="
aws ecs list-tasks --cluster three-tier-cluster --region ap-south-1 --output table

echo ""
echo "=== 3. Recent Service Events ==="
aws ecs describe-services --cluster three-tier-cluster --services backend --region ap-south-1 --query "services[0].events[0:5].message" --output table

