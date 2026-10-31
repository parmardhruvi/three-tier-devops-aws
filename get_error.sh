#!/bin/bash
export AWS_PAGER=""

echo "=== Recent ECS Service Events (Why backend:2 failed) ==="
aws ecs describe-services --cluster three-tier-cluster --services backend --region ap-south-1 --query "services[0].events[0:5].message" --output text

echo ""
echo "=== Last Stopped Task Details ==="
STOPPED_TASK=$(aws ecs list-tasks --cluster three-tier-cluster --desired-status STOPPED --region ap-south-1 --query "taskArns[0]" --output text)
if [ "$STOPPED_TASK" != "None" ] && [ -n "$STOPPED_TASK" ]; then
    echo "Stopped Task: $STOPPED_TASK"
    aws ecs describe-tasks --cluster three-tier-cluster --tasks "$STOPPED_TASK" --region ap-south-1 --query "tasks[0].{StoppedReason:stoppedReason,ContainerReason:containers[0].reason,ExitCode:containers[0].exitCode}" --output table
fi

