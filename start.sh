#!/bin/bash

echo "╔════════════════════════════════════════════════════════╗"
echo "║   🚀 Big Data Hackathon - AI Complaint System         ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""
echo "📦 Остановка старых контейнеров..."
docker-compose down

echo ""
echo "🔨 Сборка и запуск контейнеров..."
echo "   ⏳ Это может занять несколько минут при первом запуске..."
echo ""

# Build and start all services
docker-compose up --build -d

echo ""
echo "⏳ Ожидание готовности сервисов..."
echo ""

# Wait for services to be healthy
echo "   🗄️  Проверка базы данных..."
until docker exec bdh-database pg_isready -U bdh_user -d bdh_database > /dev/null 2>&1; do
  sleep 1
done
echo "   ✅ База данных готова!"

echo "   🔧 Проверка бэкенда..."
until curl -s http://localhost:8081/api/complaints/summary > /dev/null 2>&1; do
  sleep 2
done
echo "   ✅ Бэкенд готов!"

echo "   🎨 Проверка фронтенда..."
until curl -s http://localhost:5173 > /dev/null 2>&1; do
  sleep 1
done
echo "   ✅ Фронтенд готов!"

echo ""
echo "╔════════════════════════════════════════════════════════╗"
echo "║              ✅ Проект успешно запущен!                ║"
echo "╚════════════════════════════════════════════════════════╝"
echo ""
echo "🌐 Доступ к сервисам:"
echo "   Frontend:  http://localhost:5173"
echo "   Backend:   http://localhost:8081"
echo "   Database:  localhost:6789"
echo ""
echo "👥 Пользователи:"
echo "   Админ:     admin@bdh.kz / admin123"
echo "   Житель:    resident@bdh.kz / user123"
echo ""
echo "📊 Просмотр логов:"
echo "   docker-compose logs -f"
echo ""
echo "🛑 Остановка:"
echo "   ./stop.sh  или  docker-compose down"
echo ""

