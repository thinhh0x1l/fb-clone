#!/bin/bash

# Start all services
echo "Starting Facebook Clone..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running. Please start Docker Desktop."
    exit 1
fi

# Start infrastructure
echo "Starting infrastructure services..."
docker-compose up -d postgres redis minio

# Wait for services to be ready
echo "Waiting for services to be ready..."
sleep 5

# Start backend
echo "Starting backend..."
cd backend
if [ ! -f "mvnw" ]; then
    echo "Maven wrapper not found. Please run: mvn wrapper:wrapper"
    exit 1
fi
./mvnw spring-boot:run &
BACKEND_PID=$!
cd ..

# Start frontend
echo "Starting frontend..."
cd frontend
if [ ! -d "node_modules" ]; then
    echo "Installing frontend dependencies..."
    npm install
fi
npm run dev &
FRONTEND_PID=$!
cd ..

echo ""
echo "=== Facebook Clone is running ==="
echo "Frontend: http://localhost:5173"
echo "Backend API: http://localhost:8080/api/v1"
echo "MinIO Console: http://localhost:9001 (minioadmin/minioadmin)"
echo ""
echo "Press Ctrl+C to stop all services"

# Trap to cleanup on exit
trap "echo 'Stopping services...'; kill $BACKEND_PID $FRONTEND_PID; docker-compose stop; exit" SIGINT SIGTERM

wait
