# Cloud Native Microservices Platform

A modern, cloud-native microservices demo project showcasing industry best practices using Spring Boot, Spring Cloud, Kafka, MongoDB, MySQL, and Redis with Debezium CDC.

## Quick Start

### Prerequisites
- Docker & Docker Compose
- Java 17+
- Maven 3.8+

### Build & Run
```bash
# Build all services
mvn clean package -DskipTests

# Start infrastructure
docker-compose up -d

# View logs
docker-compose logs -f
```

### Create Test User
```bash
curl -X POST http://localhost:8082/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": "ROLE_USER"
  }'
```

### Get JWT Token
```bash
curl -X POST http://localhost:8081/auth/token \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1",
    "email": "john@example.com"
  }'
```

### Call Protected API
```bash
curl -H "Authorization: Bearer <JWT_TOKEN>" \
  http://localhost:8080/api/users/id/1
```

## Service Ports
- Gateway: 8080
- Auth Service: 8081
- User Service: 8082
- Order Service: 8083
- Catalog Service: 8084
- Recommendation Service: 8085
- Notification Service: 8086
- Eureka Server: 8761
- Config Server: 8888
- Prometheus: 9090
- Grafana: 3000
- Zipkin: 9411

## Architecture Highlights

### 1. Service Discovery (Eureka)
- All services auto-register with Eureka
- Gateway discovers services dynamically
- Load balancing across service instances

### 2. JWT Authentication
- Auth Service issues JWT tokens after SAML validation
- Gateway validates JWT on protected routes
- Stateless authentication across all services

### 3. Change Data Capture (CDC)
- Debezium captures user profile changes from MySQL
- Automatic event publishing to Kafka (no code changes needed)
- Events flow: User Service → MySQL → Debezium → Kafka → Recommendation Service

### 4. Event-Driven Architecture
- Order Service publishes order-events to Kafka
- Multiple consumer groups (notification, recommendation)
- Async processing with guaranteed ordering per partition key

### 5. Caching Pattern
- Catalog Service implements cache-aside with Redis
- Automatic cache invalidation on updates
- Demonstrates cache hit/miss behavior

### 6. Database Diversity
- MySQL for transactional data (users, orders)
- MongoDB for flexible schemas (products, recommendations)
- Redis for high-speed caching

## Demo Workflows

### Workflow 1: User Update → CDC → Recommendation Service
```bash
# Update user
curl -X PUT http://localhost:8082/users/1 \
  -H "Content-Type: application/json" \
  -d '{"firstName": "John", "lastName": "Updated"}'

# Debezium captures change and publishes to Kafka
# Recommendation Service consumes event and updates recommendations
```

### Workflow 2: Create Order → Notifications
```bash
# Create order
curl -X POST http://localhost:8083/orders \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "totalAmount": 99.99}'

# Order Service publishes to Kafka
# Notification Service logs email/SMS notifications
# Recommendation Service updates user behavior model
```

### Workflow 3: Product Caching
```bash
# Create product
curl -X POST http://localhost:8084/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "price": 999.99,
    "categories": ["electronics"]
  }'

# First request: Cache miss (logs: "Cache MISS")
curl http://localhost:8084/products/prod123

# Second request: Cache hit (logs: "Cache HIT")
curl http://localhost:8084/products/prod123
```

## Key Technologies

- **Framework**: Spring Boot 3.2 + Spring Cloud 2023.0
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Authentication**: JWT with SAML support
- **Databases**: MySQL (transactional), MongoDB (document)
- **Caching**: Redis with cache-aside pattern
- **Event Streaming**: Apache Kafka with Debezium CDC
- **Observability**: Prometheus, Grafana, Zipkin
- **Containerization**: Docker & Docker Compose

## Kafka Topics

| Topic | Partitions | Key | Producers | Consumers |
|-------|-----------|-----|-----------|-----------|
| `user-changes` | 6 | userId | Debezium | recommendation-service |
| `order-events` | 6 | userId | order-service | notification-service, recommendation-service |

## Architecture Diagram

```
Client → API Gateway (JWT validation) → Services
                  ↓ (Discovery)
              Eureka Server
                  ↓
    ┌─────────────┼─────────────┐
    ↓             ↓             ↓
Auth Service  User Service  Order Service
              (MySQL)       (MySQL)
                ↓ (CDC)        ↓ (Kafka)
            Debezium     Kafka Events
                ↓             ↓
            ┌───────────────────┐
            │  Kafka Broker     │
            │ user-changes      │
            │ order-events      │
            └─────────┬─────────┘
                      ↓
          ┌───────────┴───────────┐
          ↓                       ↓
   Recommendation Service   Notification Service
   (MongoDB)               (Async)
```

## Configuration Hierarchy

Each service reads config in this order:
1. `application.yml` (defaults)
2. `application-docker.yml` (Docker overrides)
3. Config Server (centralized)

## Service Details

### User Service (Port 8082)
- **Database**: MySQL (users table)
- **CDC**: Debezium publishes all changes to `user-changes` Kafka topic
- **Endpoints**: CRUD operations on user profiles
- **Events**: INSERT, UPDATE, DELETE captured automatically

### Order Service (Port 8083)
- **Database**: MySQL (orders table)
- **Producer**: Publishes `order-events` to Kafka
- **Endpoints**: Order creation, status updates, retrieval
- **Key Partition**: userId (ensures order per user)

### Catalog Service (Port 8084)
- **Database**: MongoDB (flexible product schema)
- **Cache**: Redis cache-aside pattern
- **Endpoints**: Product search, retrieval with caching
- **Cache Key**: `product:{productId}` (TTL: 1 hour)

### Recommendation Service (Port 8085)
- **Database**: MongoDB (recommendation models)
- **Consumers**: `user-changes` + `order-events` (3 concurrent threads)
- **Endpoints**: Get recommendations per user
- **Updates**: Real-time based on user/order events

### Notification Service (Port 8086)
- **Consumer**: `order-events` (3 concurrent threads)
- **Actions**: Send email/SMS/push notifications (mocked)
- **Stateless**: No persistent state, just logs

## Monitoring & Observability

- **Prometheus**: http://localhost:9090 (metrics)
- **Grafana**: http://localhost:3000 (dashboards)
- **Zipkin**: http://localhost:9411 (distributed tracing)
- **Service Health**: `/actuator/health` endpoint on each service
- **Logs**: `docker-compose logs -f` to stream all logs

## Troubleshooting

```bash
# Check all services are running
docker-compose ps

# View service logs
docker-compose logs -f <service-name>

# Check Eureka registration
curl http://localhost:8761/eureka/apps

# Verify Kafka topics
docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092

# Check Redis
docker-compose exec redis-cache redis-cli ping
```

## Production Deployment

See [PRODUCTION.md](./PRODUCTION.md) for:
- Security hardening
- Performance tuning
- Kubernetes deployment
- Distributed tracing setup
- Alert configuration

## Further Reading

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Apache Kafka](https://kafka.apache.org/documentation/)
- [Debezium CDC](https://debezium.io/)
- [MongoDB](https://docs.mongodb.com/)
- [Redis](https://redis.io/documentation)