#!/bin/bash

# Stop all services
echo "Stopping Facebook Clone..."

# Stop Docker services
docker-compose down

# Kill any running Java/Node processes
pkill -f "spring-boot:run" 2>/dev/null
pkill -f "vite" 2>/dev/null

echo "All services stopped."
