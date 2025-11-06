#!/bin/bash
# Script to start PostgreSQL database with initialization

echo "🚀 Starting PostgreSQL database..."

# Stop and remove existing containers
docker-compose down

# Remove old volume to ensure fresh start
echo "🗑️  Removing old database volume..."
docker volume rm big-data-hackathon_db_data 2>/dev/null || true

# Start database
echo "📦 Starting database container..."
docker-compose up -d db

# Wait for database to be ready
echo "⏳ Waiting for database to be ready..."
sleep 5

# Check if database is running
if docker-compose ps | grep -q "db.*Up"; then
    echo "✅ Database is running on localhost:6789"
    echo ""
    echo "📊 Database credentials:"
    echo "   Host: localhost"
    echo "   Port: 6789"
    echo "   Database: bdh_database"
    echo "   Username: bdh_user"
    echo "   Password: hackathon2024"
    echo ""
    echo "👥 Test users:"
    echo "   Admin: admin / admin123"
    echo "   Resident: resident / user123"
    echo ""
    echo "📋 To view logs: docker-compose logs -f db"
    echo "🛑 To stop: docker-compose down"
else
    echo "❌ Failed to start database"
    docker-compose logs db
    exit 1
fi

