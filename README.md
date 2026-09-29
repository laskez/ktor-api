# Ktor Task API
Сервер на Ktor c CRUD для управления списком задач и JWT-аутентификацией

## Стек

- Kotlin 2.3.0
- Ktor 3.6.0
- kotlinx.serialization + ContentNegotiation
- JWT
- BCrypt
- Gradle, JDK 21

## Запуск

```
.\gradlew.bat run
```

Сервер на http://localhost:8080

## Аутентификация
Часть маршрутов защищена JWT-токеном:
1. Зарегистрироваться через `/auth/register`
2. Войти через `/auth/login` - получить токен
3. Передавать его в заголовке `Authorization: Bearer <токен>`

### Регистрация
```bash
curl -i -X POST http://localhost:8080/auth/register \
-H "Content-Type: application/json" \
-d '{"login":"student","password":"pass123"}'
```
Ответ `201` - `{"id":1, "login":"student"}`. Если логин занят - `409`, если пустой логин или пароль - `400`.

### Вход

```bash
curl -i -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"login":"student","password":"pass123"}'
```

Ответ `200` - `{"token":"eyJhbGciOi..."}`. Если логин или пароль неверный - `401`.

## Маршруты

| Метод  | Путь               | Что делает         | Доступ    |
|--------|--------------------|--------------------|-----------|
| GET    | `/tasks`           | Все задачи         | публичный |
| GET    | `/tasks?done=true` | Только выполненные | публичный | 
| GET    | `/tasks/{id}`      | Одна задача        | публичный | 
| POST   | `/tasks`           | Создать            | нужен JWT |
| PUT    | `/tasks/{id}`      | Обновить           | нужен JWT |
| DELETE | `/tasks/{id}`      | Удалить            | нужен JWT |

## Примеры

```bash
# Список всех задач 
curl http://localhost:8080/tasks

# Создать задачу
curl -i -X POST http://localhost:8080/tasks \
-H "Authorization: Bearer <ТОКЕН>" \
-H "Content-Type: application/json" \
-d '{"title":"Новая задача"}'

# Обновить задачу. Нужен токен
curl -i -X PUT http://localhost:8080/tasks/1 \
-H "Authorization: Bearer <ТОКЕН>" \
-H "Content-Type: application/json" \
-d '{"done":true}'

# Удалить задачу по id. Нужен токен
curl -i -X DELETE http://localhost:8080/tasks/1 \
-H "Authorization: Bearer <ТОКЕН>"

# Запрос без токена на защищенный маршрут
curl -i -X POST http://localhost:8080/tasks \
-H "Content-Type: application/json" \
-d '{"title":"Без токена"}'
```

## Коды ответов

| Код | Когда                                         |
|-----|-----------------------------------------------|
| 200 | Успешный GET или PUT                          |
| 201 | Задача создана / пользователь зарегистрирован |
| 204 | Задача удалена                                |
| 400 | Некорректный id, JSON или пустые поля         |
| 404 | Задача не найдена                             |
| 409 | Логин уже занят                               |

