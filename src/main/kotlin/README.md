# Ktor Task API
Сервер на Ktor для управления списком задач

## Стек

- Kotlin 2.3.0
- Ktor 3.6.0
- kotlinx.serialization + ContentNegotiation
- Gradle, JDK 21

## Запуск

```
.\gradlew.bat run
```

Сервер на http://localhost:8080

## Маршруты

| Метод  | Путь               | Что делает         |
|--------|--------------------|--------------------|
| GET    | `/tasks`           | Все задачи         |
| GET    | `/tasks?done=true` | Только выполненные |
| GET    | `/tasks/{id}`      | Одна задача        |
| POST   | `/tasks`           | Создать            |
| DELETE | `/tasks/{id}`      | Удалить            |

## Примеры

```bash
# Список всех задач 
curl http://localhost:8080/tasks

# Создать задачу
curl -i -X POST http://localhost:8080/tasks \
-H "Content-Type: application/json" \
-d '{"title":"Новая задача"}'

# Удалить задачу по id
curl -i -X DELETE http://localhost:8080/tasks/1
```

## Коды ответов

| Код | Когда                    |
|-----|--------------------------|
| 200 | Успешный GET             |
| 201 | Задача создана           |
| 204 | Задача удалена           |
| 400 | Некорректный id или JSON |
| 404 | Задача не найдена        |

