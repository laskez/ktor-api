# Ktor Task API
Сервер на Ktor c CRUD для управления списком задач и JWT-аутентификацией

## Стек

![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Ktor](https://img.shields.io/badge/Ktor-3.6.0-087CFA?style=for-the-badge&logo=ktor&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Gradle](https://img.shields.io/badge/Gradle-Build-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![JDK](https://img.shields.io/badge/JDK-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)

## Запуск

```
.\gradlew.bat run
```

Сервер на http://localhost:8080

## Swagger UI

Интерактивная документация API доступна по адресу:

```
http://localhost:8080/swagger
```

Там описаны все маршруты с параметрами, схемами запросов и ответов. 
Можно нажать **Try it out** и отправить запрос прямо из браузера.
Для защищенных маршрутов - кнопка **Authorize** сверху, куда вставляется JWT-токен.

Спецификация OpenAPI лежит в `src/main/resources/openapi/documentation.yaml`

## Тесты

Запуск всех тестов:

```bash
.\gradlew.bat test
```

Покрыто 13 тестов:
- публичные GET-маршруты (`/tasks`,`/tasks/{id}`, фильтр `?done=true`, ошибки 400/404)
- регистрация (успех, занятый логин, пустой логин)
- вход (успех, неверный пароль)
- защита маршрутов (401 без токена, 201 с токеном, 204 при удалении)

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

| Код  | Когда                                         |
|------|-----------------------------------------------|
| 200  | Успешный GET или PUT                          |
| 201  | Задача создана / пользователь зарегистрирован |
| 204  | Задача удалена                                |
| 400  | Некорректный id, JSON или пустые поля         |
| 401  | Неверный пароль или отсутствует токен         |
| 404  | Задача не найдена                             |
| 409  | Логин уже занят                               |

## Обработка ошибок и логирование

- **StatusPages** - централизованная обработка исключений. 
Некорректный JSON в теле запроса возвращает `400` с описанием, любое необработанное исключение - `500`.
Отдельные `try/catch` в маршрутах не нужны.
- **CallLogging** - каждый входящий запрос логируется в консоль в формате `METHOD PATH -> STATUS`.
Уровень логирования - `INFO` (настраивается в `src/main/resources/logback.xml`)