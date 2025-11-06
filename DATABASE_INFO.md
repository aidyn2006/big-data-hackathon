# База данных PostgreSQL

## Credentials для локальной разработки

```
Host: localhost (или host.docker.internal если приложение в Docker)
Port: 6789
Database: bdh_database
Username: bdh_user
Password: hackathon2024
```

## Подключение через JDBC

**Для запуска вне Docker (локально):**
```
jdbc:postgresql://localhost:6789/bdh_database?preferQueryMode=simple&autosave=always&prepareThreshold=0
```

**Для запуска внутри Docker:**
```
jdbc:postgresql://host.docker.internal:6789/bdh_database?preferQueryMode=simple&autosave=always&prepareThreshold=0
```

## Запуск базы данных

### Через Docker Compose (рекомендуется)

```bash
# Запустить только базу данных
docker-compose up -d db

# Проверить статус
docker-compose ps

# Посмотреть логи
docker-compose logs db

# Остановить
docker-compose down
```

### Подключение через psql

```bash
psql -h localhost -p 6789 -U bdh_user -d bdh_database
# Пароль: hackathon2024
```

### Подключение через DBeaver / DataGrip

1. Создать новое подключение PostgreSQL
2. Host: `localhost`
3. Port: `6789`
4. Database: `bdh_database`
5. Username: `bdh_user`
6. Password: `hackathon2024`

## Структура таблиц

### Таблица `users`
Хранит информацию о пользователях системы (жители и администраторы).

| Колонка | Тип | Описание |
|---------|-----|----------|
| id | BIGSERIAL | Первичный ключ |
| username | VARCHAR(50) | Уникальное имя пользователя |
| password | VARCHAR(255) | Хешированный пароль (BCrypt) |
| email | VARCHAR(255) | Email пользователя |
| enabled | BOOLEAN | Активен ли аккаунт |
| created_at | TIMESTAMP | Дата создания |

### Таблица `user_roles`
Хранит роли пользователей (связь многие-ко-многим).

| Колонка | Тип | Описание |
|---------|-----|----------|
| user_id | BIGINT | FK на users.id |
| role | VARCHAR(50) | Роль: USER или ADMIN |

### Таблица `complaints`
Хранит жалобы с полями для AI-анализа.

| Колонка | Тип | Описание |
|---------|-----|----------|
| id | UUID | Первичный ключ |
| raw_text | TEXT | Исходный текст жалобы |
| route | TEXT | Номер маршрута |
| object | TEXT | Объект жалобы (транспорт, остановка и т.д.) |
| time | TIMESTAMP WITH TIME ZONE | Время инцидента |
| place | TEXT | Место инцидента |
| actor | TEXT | Участник (водитель, кондуктор и т.д.) |
| aspect | TEXT[] | Массив аспектов проблемы |
| priority | TEXT | Приоритет (критический, высокий, средний, низкий) |
| evidence | TEXT[] | Массив доказательств |
| confidence | DOUBLE PRECISION | Уверенность AI (0.0 - 1.0) |
| created_by | VARCHAR(100) | Имя пользователя, создавшего жалобу |
| status | VARCHAR(32) | Статус (NEW, IN_PROGRESS, RESOLVED и т.д.) |
| latitude | DOUBLE PRECISION | Широта |
| longitude | DOUBLE PRECISION | Долгота |
| user_id | BIGINT | ID пользователя |
| created_at | TIMESTAMP WITH TIME ZONE | Дата создания |
| updated_at | TIMESTAMP WITH TIME ZONE | Дата обновления |

### Индексы
- `idx_complaints_route` - по маршруту
- `idx_complaints_priority` - по приоритету
- `idx_complaints_actor` - по участнику
- `idx_complaints_place` - по месту
- `idx_complaints_created_by` - по автору
- `idx_complaints_status` - по статусу
- `idx_complaints_user_id` - по ID пользователя
- `idx_complaints_created_at` - по дате создания

## Тестовые пользователи

После инициализации базы созданы 2 тестовых пользователя:

| Username | Password | Email | Role |
|----------|----------|-------|------|
| admin | admin123 | admin@bdh.kz | ADMIN |
| resident | user123 | resident@bdh.kz | USER |

## Примечания

- База данных использует порт **6789** вместо стандартного 5432, чтобы не конфликтовать с другими PostgreSQL инстансами
- Данные хранятся в Docker volume `big-data-hackathon_db_data`
- При первом запуске Hibernate автоматически создаст все необходимые таблицы (`spring.jpa.hibernate.ddl-auto=update`)

