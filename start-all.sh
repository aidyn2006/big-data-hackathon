#!/bin/bash
# Script to start all services (database, backend, frontend)

echo "🚀 Starting all services..."

# Stop existing containers
docker-compose down

# Build and start all services
echo "🏗️  Building and starting services..."
docker-compose up -d --build

# Wait for services to be ready
echo "⏳ Waiting for services to start..."
sleep 10

# Check status
echo ""
echo "📊 Services status:"
docker-compose ps

echo ""
echo "✅ Services are starting!"
echo ""
echo "🌐 Access points:"
echo "   Frontend: http://localhost:5173"
echo "   Backend:  http://localhost:8080"
echo "   Database: localhost:6789"
echo ""
echo "📋 To view logs: docker-compose logs -f"
echo "🛑 To stop all: docker-compose down"


