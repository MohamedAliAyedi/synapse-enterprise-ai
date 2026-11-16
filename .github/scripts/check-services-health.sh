#!/usr/bin/env bash
set -euo pipefail

# List of service health URLs
declare -A services=(
  ["api-gateway"]="http://localhost:8080/actuator/health"
  ["discovery-server"]="http://localhost:8761/eureka/apps"
  ["ai-orchestrator-service"]="http://localhost:8081/actuator/health"
  ["document-indexer-service"]="http://localhost:8082/actuator/health"
  ["notification-service"]="http://localhost:8083/actuator/health"
)

MAX_RETRIES=30
SLEEP_SECONDS=5

echo "Waiting for microservices to become healthy..."

for service in "${!services[@]}"; do
  health_url="${services[$service]}"
  retries=0
  until curl -s -f "$health_url" >/dev/null; do
    ((retries++))
    if (( retries >= MAX_RETRIES )); then
      echo "❌ $service did not become healthy after $((MAX_RETRIES * SLEEP_SECONDS)) seconds."
      exit 1
    fi
    echo "⏳ Waiting for $service ($health_url)..."
    sleep "$SLEEP_SECONDS"
  done
  echo "✅ $service is healthy."
done

echo "All services are healthy."
