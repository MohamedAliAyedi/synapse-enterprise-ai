#!/usr/bin/env bash
set -euo pipefail

# Start all microservices defined in docker-compose.yml
docker compose up -d

# List of health check endpoints for the services
declare -A services=(
  [api-gateway]="http://localhost:8080/actuator/health"
  [discovery-server]="http://localhost:8761/actuator/health"
  [ai-orchestrator-service]="http://localhost:8082/actuator/health"
  [document-indexer-service]="http://localhost:8083/actuator/health"
  [notification-service]="http://localhost:8084/actuator/health"
)

# Maximum wait time (seconds) for all services to become healthy
MAX_WAIT=180
START_TIME=$(date +%s)

echo "Waiting for services to become healthy..."
while true; do
  ALL_OK=true
  for name url in "${!services[@]}"; do
    health_url=${services[$name]}
    if curl -s --fail "$health_url" > /dev/null; then
      echo "✅ $name is healthy"
    else
      echo "⏳ $name not ready yet"
      ALL_OK=false
    fi
  done

  if $ALL_OK; then
    echo "All services are healthy."
    break
  fi

  CURRENT_TIME=$(date +%s)
  ELAPSED=$((CURRENT_TIME - START_TIME))
  if [ $ELAPSED -ge $MAX_WAIT ]; then
    echo "❌ Timeout after $MAX_WAIT seconds waiting for services."
    docker compose logs
    exit 1
  fi
  sleep 5
done
