#!/bin/bash
# Script to stop all services

echo "🛑 Stopping all services..."
docker-compose down

echo "✅ All services stopped"
echo ""
echo "💡 To remove database data: docker volume rm big-data-hackathon_db_data"

