# Поликлиника

Веб-приложение для управления пациентами, врачами, расписанием приёма и записями на приём.

## Технологии

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL 17
- Maven
- Docker и Docker Compose
- HTML, CSS, JavaScript

## Возможности

- Создание и просмотр пациентов.
- Создание и просмотр врачей.
- Просмотр расписания врача за месяц.
- Запись пациента на приём.


## Запуск через Docker

### Требования

- Docker Desktop с поддержкой Docker Compose.
- Git.

### 1. Клонирование проекта

```bash
git clone <URL-репозитория>
cd polyclinic
```

### 2. Настройка переменных окружения

Создайте файл `.env` на основе примера:

В PowerShell:

```powershell
Copy-Item .env.example .env
```

Укажите в `.env` надёжный пароль для PostgreSQL.

### 3. Запуск приложения

```bash
docker compose up --build -d
```

### 4. Открытие приложения

Веб-интерфейс:

http://localhost:8080

Проверка API:

http://localhost:8080/api/health


### 5. Просмотр логов

```bash
docker compose logs -f app
```

Логи PostgreSQL:

```bash
docker compose logs -f db
```

### 6. Остановка приложения

```bash
docker compose down
```

Данные PostgreSQL хранятся в Docker volume и сохраняются при обычной остановке контейнеров.

**Внимание:** команда `docker compose down -v` удаляет том базы данных.

## Структура проекта

- `src/main/java` — Java-код приложения.
- `src/main/resources/static` — HTML, CSS и JavaScript.
- `src/main/resources/application.properties` — настройки Spring Boot.
- `database/schema.sql` — SQL-схема базы данных.
- `Dockerfile` — сборка образа приложения.
- `docker-compose.yaml` — запуск приложения и PostgreSQL.
- `pom.xml` — зависимости и настройки Maven.

## База данных

База данных хранит информацию об участках, пациентах, врачах, расписании врачей и записях на приём.

Структура и ограничения базы данных определены в SQL-схеме проекта.