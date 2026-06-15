@echo off
REM Start all services for Windows

echo Starting Facebook Clone...

REM Check if Docker is running
docker info > nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Docker is not running. Please start Docker Desktop.
    exit /b 1
)

REM Start infrastructure
echo Starting infrastructure services...
docker-compose up -d postgres redis minio

REM Wait for services
echo Waiting for services to be ready...
timeout /t 5 /nobreak > nul

REM Start backend in new window
echo Starting backend...
start "Facebook Backend" cmd /k "cd backend && mvnw spring-boot:run"

REM Start frontend in new window
echo Starting frontend...
start "Facebook Frontend" cmd /k "cd frontend && npm run dev"

echo.
echo === Facebook Clone is running ===
echo Frontend: http://localhost:5173
echo Backend API: http://localhost:8080/api/v1
echo MinIO Console: http://localhost:9001 (minioadmin/minioadmin)
echo.
pause
