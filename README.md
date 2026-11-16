# Книги та Автори — курсова робота (КМ-41)

Поліглотне сховище: **MongoDB (sharded, 3 шарди)** + **Elasticsearch** + **Redis**, Spring Boot 3 (Java 17),
гексагональна архітектура + CQRS.

## Запуск

1. Покладіть повний датасет у `data/books_1.Best_Books_Ever.csv` (Goodreads «Best Books Ever»).
   Невеликий `data/books_sample.csv` залишено для швидкої перевірки (у UI вкажіть шлях `/app/data/books_sample.csv`).
2. `docker compose up --build`
   (потрібно ≈3–4 ГБ ОЗП: 3 шарди + config server + mongos + Elasticsearch + Redis + застосунок).
3. Відкрийте http://localhost:8080 → слайдер швидкості → «Запустити зчитування CSV».

Старт займає 1–2 хв: спочатку `cluster-init` (replica sets) → `mongos` → `shard-init` (addShard + шардінг) → `app`.

## Що перевірити на захисті

```bash
# розподіл документів по шардах (завдання 13)
docker exec -it books-mongos mongosh --quiet --eval "db.getSiblingDB('booksdb').books.getShardDistribution()"
# статус шардування
docker exec -it books-mongos mongosh --quiet --eval "sh.status()"
# кількість документів в Elasticsearch та приклад пошуку
curl "localhost:9200/books/_count"
# ключі кешу в Redis
docker exec -it books-redis redis-cli keys "search:*"
# агрегація по шардах (завдання 14) та розподіл
curl localhost:8080/api/stats/by-genre ; curl localhost:8080/api/stats/shards
```

Логи пишуться в `./logs/app.log` (том), тому лишаються після `docker compose down`.
Рівень логування: `APP_LOG_LEVEL=INFO|DEBUG|WARN|ERROR` у `docker-compose.yml` (DEBUG показує час bulk-запитів до кожної БД).

## Тести

`mvn test` — запис (повнота полів, ідемпотентність, збій індексації), пошук (позитивний за унікальним маркером,
негативний — порожній результат без помилки), нормалізація запитів/кеш, парсинг CSV (багаторядкові описи, Python-списки).
Тести використовують in-memory реалізації портів (гексагональна архітектура це дозволяє) і не потребують Docker.

## Архітектура

```
UI (index.html) → BookController / AdminController   (presentation)
                → порти IN: SearchQueryPort, FilterBooksQueryPort, IngestionControlPort, StatsQueryPort
                → use case-и (application): SearchBooksUseCase, FilterBooksUseCase, IngestBookUseCase, IngestionService, StatsUseCase
                → порти OUT: BookRepositoryPort, SearchIndexPort, CachePort, SearchStatsPort, BookSourcePort
                → адаптери (infrastructure): MongoBookRepositoryAdapter, ElasticsearchSearchAdapter,
                                              RedisCacheAdapter, RedisSearchStatsAdapter, CsvBookSourceAdapter
```

* **Запис (command):** CSV → `CsvBookSourceAdapter` → `IngestionService` (пакети кожні 100 мс, швидкість 10…5000/с)
  → `IngestBookUseCase` → bulk upsert у MongoDB (через mongos) → bulk-індексація в Elasticsearch.
* **Читання (query):** запит → нормалізація (`QueryNormalizer`: нижній регістр, без розділових знаків, слова відсортовані)
  → Redis (TTL 60 с) → при miss Elasticsearch (multi_match + підсвічування) → результат кешується.
* **Шардінг:** `{_id: "hashed"}`, 6 початкових чанків на 3 шарди. `_id` = `bookId` з датасету (або хеш назви й автора).
* **Агрегація по шардах:** `$unwind → $group → $sort` виконується на кожному шарді, mongos зливає часткові результати
  (аналог map-reduce); розподіл по шардах береться з `$collStats`.

## Що змінено порівняно з попередньою версією

| Було | Стало |
|---|---|
| Самописний парсер CSV ламався на багаторядкових описах (сміття в назвах, 4697 записів із `Unknown`) | Apache Commons CSV, коректний розбір лапок, списків Python, дат |
| Redis, Elasticsearch — заглушки в пам'яті, залежностей у `pom.xml` не було | Реальні Redis і Elasticsearch (compose + код), індекс і кеш переживають рестарт |
| Один контейнер `mongo`, шардингу не було | Config server + 3 шарди + mongos, `shardCollection` автоматично |
| Повторний імпорт дублював книги | Upsert за стабільним `_id` |
| Контролер працював напряму з `MongoTemplate` та адаптерами | Контролери залежать лише від вхідних портів |
| `/api/books` без параметрів повертав усю колекцію | Пагінація (`page`, `size`), індекси Mongo |
| Нескінченний кеш порожніх відповідей | TTL у Redis |
| Тестів немає | 4 тестові класи |
| Логування лише через змінну в compose, INFO на кожен запис | `application.yml`, рівні через `APP_LOG_LEVEL`, ротація, прогрес раз на 5 с |
| UI без статистики пошуку | Статистика запису/пошуку, cache hit %, останні запити зі статусом, підсвічування, агрегація по жанрах, розподіл по шардах, перехід до книг автора |

## Відомі обмеження (для розділу звіту)

* Кожен шард — replica set з одного вузла (реплікація — бонусне завдання; для неї достатньо додати ще по 2 вузли на шард).
* Neo4j не використовується (раніше був лише порожній клас). Зв'язок «автор → книги» реалізовано фільтром по автору в MongoDB.
* Окремих колекцій `authors`/`publishers` немає: автор і видавництво — поля документа книги.
* Біографії авторів не генеруються (у датасеті їх немає); пошук іде по назві, автору та анотації.
* Elasticsearch — один вузол без реплік (у звіті має бути вказано так само).
* Інтеграційних тестів із реальними БД (Testcontainers) немає.
