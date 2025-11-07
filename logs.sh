#!/bin/bash

echo "📊 Просмотр логов контейнеров..."
echo ""
echo "Выберите сервис:"
echo "  1) Все сервисы"
echo "  2) База данных"
echo "  3) Бэкенд"
echo "  4) Фронтенд"
echo ""
read -p "Ваш выбор (1-4): " choice

case $choice in
  1)
    echo "📋 Логи всех сервисов:"
    docker-compose logs -f
    ;;
  2)
    echo "📋 Логи базы данных:"
    docker-compose logs -f db
    ;;
  3)
    echo "📋 Логи бэкенда:"
    docker-compose logs -f backend
    ;;
  4)
    echo "📋 Логи фронтенда:"
    docker-compose logs -f frontend
    ;;
  *)
    echo "❌ Неверный выбор"
    exit 1
    ;;
esac


