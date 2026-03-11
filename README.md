# 🛒 IT Shop — Интернет-магазин ИТ-услуг и оборудования

Микросервисный e-commerce проект, интегрированный с CRM-системой через Apache Kafka.  
**Live demo:** [bolohonovma.online/shop](https://bolohonovma.online/shop)

## Архитектура

```
┌────────────────────────────────────┐     ┌────────────────────────────────────┐
│          SHOP (namespace: shop)     │     │          CRM (namespace: crm)       │
│                                     │     │                                     │
│  ┌──────────┐    ┌──────────────┐  │     │  ┌──────────────┐  ┌────────────┐  │
│  │  Vue 3   │───▶│ shop-backend │  │     │  │ crm-backend  │──│ Vue 3 +    │  │
│  │ Tailwind │    │  Spring Boot │  │     │  │ Spring Boot  │  │ PrimeVue   │  │
│  └──────────┘    └──────┬───────┘  │     │  └──────┬───────┘  └────────────┘  │
│                         │          │     │         │                           │
│               ┌─────────┼────────┐ │     │  ┌──────┼─────────┐                │
│               ▼         ▼        ▼ │     │  ▼      ▼         ▼                │
│          ┌────────┐ ┌──────┐     │ │     │ ┌────────┐ ┌──────┐               │
│          │shop_db │ │Redis │     │ │     │ │crm_db  │ │Redis │               │
│          │(PgSQL) │ │cache │     │ │     │ │(PgSQL) │ │      │               │
│          └────────┘ └──────┘     │ │     │ └────────┘ └──────┘               │
│                                  │ │     │         │                          │
│                    ┌─────────────┘ │     │         └──────────────┐           │
│                    ▼               │     │                        ▼           │
│     ┌──────────────────────────────┼─────┼────────────────────────────┐       │
│     │              Apache Kafka                                       │       │
│     │                                                                 │       │
│     │  shop.orders.created ──────────────────▶  (CRM consumer)       │       │
│     │  crm.orders.status_changed  ◀──────────  (CRM producer)        │       │
│     │  crm.products.sync  ◀─────────────────  (CRM producer)         │       │
│     │  crm.tenant.created  ◀─────────────────  (CRM producer)        │       │
│     └─────────────────────────────────────────────────────────────────┘       │
└────────────────────────────────────┘     └────────────────────────────────────┘
```

## Kafka-топики

| Топик | Направление | Описание |
|-------|-------------|----------|
| `shop.orders.created` | Shop → CRM | Новый заказ из магазина |
| `crm.orders.status_changed` | CRM → Shop | Смена статуса заказа |
| `crm.products.sync` | CRM → Shop | Синхронизация каталога товаров |
| `crm.tenant.created` | CRM → Shop | Привязка CRM-тенанта к пользователю магазина |

## Стек технологий

### Backend (shop-backend)
- **Java 21 + Spring Boot 3.4**
- **Spring Data JDBC** — простой доступ к данным
- **PostgreSQL 16** — отдельная БД магазина (shop_db)
- **Redis** — кеш каталога товаров
- **Apache Kafka** — асинхронная интеграция с CRM
- **Transactional Outbox** — гарантия доставки сообщений
- **JWT** — аутентификация покупателей
- **SSE** — real-time уведомления о статусе заказов
- **Thymeleaf** — email шаблоны
- **Springdoc OpenAPI** — Swagger UI

### Frontend (shop-frontend)
- **Vue 3 + Composition API + TypeScript**
- **Vite** — сборка
- **Pinia** — state management
- **Tailwind CSS** — стилизация
- **Axios** — HTTP клиент
- **Vue Router** — маршрутизация

### Infrastructure
- **Docker Compose** — локальная разработка
- **Helm 3** — деплой в k3s
- **Liquibase** — миграции БД
- **Mailhog** — тестирование email (dev)

## Структура проекта

```
shop-project/
├── shop-backend/              # Spring Boot API
│   ├── src/main/java/com/shop/
│   │   ├── auth/              # JWT авторизация
│   │   ├── product/           # Каталог товаров + Redis кеш
│   │   ├── cart/              # Корзина
│   │   ├── order/             # Заказы + checkout
│   │   ├── payment/           # Мок-платежи (баланс)
│   │   ├── kafka/             # Producer/Consumer/Outbox
│   │   ├── sse/               # SSE уведомления + Email
│   │   └── common/            # Config, exceptions
│   └── src/main/resources/
│       ├── application.yml
│       ├── db/                # Liquibase миграции
│       └── templates/         # Email шаблоны
├── shop-frontend/             # Vue 3 SPA
│   └── src/
│       ├── api/               # Axios API модули
│       ├── components/        # Vue компоненты
│       ├── composables/       # SSE, toast
│       ├── router/            # Vue Router
│       ├── stores/            # Pinia stores
│       ├── types/             # TypeScript типы
│       └── views/             # Страницы
├── shop-helm/                 # Helm chart для k3s
├── crm-patch/                 # Патчи для CRM
│   ├── V114__products_image_url.xml  # Миграция: image_url
│   └── ProductSyncProducer.java      # Producer для crm.products.sync
├── docker-compose.yml         # Локальная разработка
└── README.md
```

## Запуск

### Локальная разработка (Docker Compose)

```bash
# Запустить инфраструктуру
docker-compose up -d shop-postgres shop-redis kafka mailhog

# Запустить backend
cd shop-backend && mvn spring-boot:run

# Запустить frontend (в другом терминале)
cd shop-frontend && npm install && npm run dev
```

Доступы:
- Frontend: http://localhost:5174
- Backend API: http://localhost:8081/api/v1
- Swagger UI: http://localhost:8081/api/v1/swagger-ui.html
- Mailhog (email): http://localhost:8025

### Деплой в k3s

```bash
# Собрать образы
docker build -t registry.local/shop-backend:1.0.0 shop-backend/
docker build -t registry.local/shop-frontend:1.0.0 shop-frontend/

# Деплой
helm install shop shop-helm/ --namespace shop --create-namespace
```

## API Endpoints

### Публичные (без авторизации)
| Метод | URL | Описание |
|-------|-----|----------|
| POST | `/auth/register` | Регистрация |
| POST | `/auth/login` | Вход |
| POST | `/auth/refresh` | Обновление токена |
| GET | `/products` | Каталог (поиск, фильтры, пагинация) |
| GET | `/products/{id}` | Карточка товара |
| GET | `/products/categories` | Список категорий |

### Требуют авторизации (Bearer JWT)
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/auth/me` | Профиль пользователя |
| GET | `/cart` | Содержимое корзины |
| POST | `/cart/items` | Добавить товар |
| PUT | `/cart/items/{productId}` | Изменить количество |
| DELETE | `/cart/items/{productId}` | Удалить из корзины |
| DELETE | `/cart` | Очистить корзину |
| POST | `/orders/checkout` | Оформить заказ |
| GET | `/orders` | Список заказов |
| GET | `/orders/{id}` | Детали заказа |
| GET | `/payments/balance` | Баланс |
| POST | `/payments/topup` | Пополнить баланс |
| GET | `/events/subscribe` | SSE подписка |

## Жизненный цикл заказа

```
1. Покупатель оформляет заказ в магазине
   → Списывается баланс
   → Создаётся запись в orders (status=NEW)
   → Kafka outbox: shop.orders.created

2. CRM получает заказ (ShopOrderConsumer)
   → Создаёт заказ и привязывает к клиенту
   → Менеджер меняет статус: NEW → PICKING → SHIPPED → DELIVERED

3. Каждая смена статуса в CRM:
   → Kafka outbox: crm.orders.status_changed
   → Shop получает (OrderStatusConsumer)
   → Обновляет статус в shop.orders
   → SSE push клиенту (real-time)
   → Email уведомление
```

## Тестирование Kafka-интеграции (Shop ↔ CRM)

Для полноценной проверки сквозного флоу Shop → CRM → Shop необходимо использовать **одинаковый email** при регистрации в обоих сервисах. Это связано с тем, что CRM при активации аккаунта отправляет событие `crm.tenant.created` с email администратора, а Shop по этому email привязывает CRM-тенант к пользователю магазина — чтобы заказы попадали в нужную CRM-схему.

### Шаги для проверки

**1. Регистрация в CRM**

Зарегистрируйся на `https://bolohonovma.online/crm` (или через Swagger `/crm/api/v1/swagger-ui.html`):
```json
POST /auth/register
{
  "email": "your@email.com",
  "password": "password123",
  "firstName": "Имя",
  "lastName": "Фамилия",
  "phone": "+79001234567",
  "userType": "ADMIN"
}
```
Подтверди email по ссылке из письма. CRM опубликует событие `crm.tenant.created` в Kafka.

**2. Регистрация в Shop с тем же email**

Зарегистрируйся на `https://bolohonovma.online/shop` (или через Swagger `/shop/api/v1/swagger-ui.html`) используя **тот же email**:
```json
POST /auth/register
{
  "email": "your@email.com",
  "password": "password123",
  "firstName": "Имя",
  "lastName": "Фамилия",
  "phone": "+79001234567"
}
```
Shop получит событие `crm.tenant.created` и запишет `crmTenantSchema` в профиль пользователя.

> ⚠️ Порядок регистрации важен: сначала CRM, потом Shop. Если сначала зарегистрироваться в Shop — CRM-тенант будет сохранён в таблицу `pending_crm_tenants` и привяжется автоматически при получении события.

**3. Оформление заказа в Shop**

Залогинься в Shop, добавь товар в корзину и оформи заказ. Shop опубликует событие `shop.orders.created`.

**4. Проверка в CRM**

Залогинься в CRM — заказ должен появиться в разделе «Заказы» с привязанным клиентом (email покупателя).

**5. Смена статуса в CRM → обновление в Shop**

Измени статус заказа в CRM. Shop получит событие `crm.orders.status_changed` и обновит статус в реальном времени через SSE.

### Диагностика

```bash
# Статус Kafka outbox в Shop
docker exec postgres psql -U shop_user -d shop_db -c \
  "SELECT topic, status, attempt_count FROM kafka_outbox ORDER BY created_at DESC LIMIT 5;"

# Idempotency log в CRM (подтверждение получения заказа)
docker exec postgres psql -U crm_user -d crm_db -c \
  "SELECT shop_order_uuid, crm_order_id, processed_at FROM public.kafka_idempotency_log ORDER BY processed_at DESC LIMIT 5;"

# CRM-тенант привязан к пользователю Shop
docker exec postgres psql -U shop_user -d shop_db -c \
  "SELECT email, crm_tenant_schema FROM public.users WHERE email = 'your@email.com';"

# Consumer group lag
docker exec shared-kafka /opt/kafka/bin/kafka-consumer-groups.sh \
  --bootstrap-server localhost:9092 --group crm-backend --describe
```

## Демо-данные

25 товаров в категориях: Разработка, DevOps, Cloud, Лицензии, Серверы,
Сетевое оборудование, Безопасность, Компьютеры, Консалтинг, Маркетинг.

При регистрации каждый пользователь получает виртуальный баланс **1 000 000 ₽**.

## Патчи для CRM

Файлы из `crm-patch/` нужно интегрировать в CRM:

1. **V114__products_image_url.xml** → скопировать в crm-liquibase миграции
2. **ProductSyncProducer.java** → добавить в crm-backend, вызывать из ProductService
3. Добавить `crm.products.sync` в конфигурацию Kafka топиков CRM
