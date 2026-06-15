@echo off
REM Stop all services for Windows

echo Stopping Facebook Clone...

REM Stop Docker services
docker-compose down

echo All services stopped.
pause
