# SOA Lab Course - Микросервисная архитектура с Service Discovery

## Описание проекта

Проект представляет собой распределённую систему управления организациями и сотрудниками, построенную на основе микросервисной архитектуры с использованием паттернов Service Discovery, API Gateway и Centralized Configuration.

---

## Архитектура системы

### Общая схема

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              FRONTEND (NGINX)                                   │
│                         https://localhost:8448                                  │
└────────────────┬────────────────────────────────────┬───────────────────────────┘
                 │                                    │
          /soa/api/v1/*                      /orgmanager/api/v1/*
                 │                                    │
                 ▼                                    ▼
    ┌────────────────────────┐          ┌────────────────────────┐
    │     CALLED-WEB         │          │    ZUUL GATEWAY        │
    │   (JAX-RS/WildFly)     │          │  (Netflix Zuul Proxy)  │
    │   Port: 8443 (HTTPS)   │          │    Port: 8080 (HTTP)   │
    └──────────┬─────────────┘          └──────────┬─────────────┘
               │                                   │
        Consul Discovery                    Eureka Discovery
               │                                   │
               ▼                                   ▼
    ┌────────────────────────┐          ┌────────────────────────┐
    │     CALLED-EJB         │          │   CALLER-SERVICE       │
    │   (EJB 3.2/WildFly)    │          │   (Spring Boot REST)   │
    │   Port: 8080 (HTTP)    │          │    Port: 8080 (HTTP)   │
    └──────────┬─────────────┘          └──────────┬─────────────┘
               │                                   │
               │                                   │
               ▼                                   ▼
         PostgreSQL DB                    CALLED-WEB (HTTPS)
                                                   │
                                            Consul Discovery
                                                   │
                                                   ▼
                                            CALLED-EJB

    ┌──────────────────────────────────────────────────────────┐
    │         INFRASTRUCTURE SERVICES                          │
    ├──────────────────────────────────────────────────────────┤
    │  • Consul (Service Discovery для WildFly)                │
    │  • Eureka (Service Discovery для Spring Cloud)           │
    │  • Config Server (Централизованная конфигурация)         │
    │  • PostgreSQL (База данных)                              │
    └──────────────────────────────────────────────────────────┘
```

---

## Компоненты системы

### 1. **Frontend (NGINX)**

**Роль:** Точка входа для клиентских запросов, маршрутизация на backend сервисы.

**Технологии:**
- NGINX 1.25 с поддержкой HTTP/2
- TLS 1.2/1.3 шифрование
- Reverse Proxy

**Функции:**
- Обслуживание статического React фронтенда
- Проксирование запросов на backend:
  - `/soa/api/v1/*` → `called-web:8443` (прямое подключение)
  - `/orgmanager/api/v1/*` → `zuul-gateway:8080` (через API Gateway)
- SSL терминация
- Балансировка нагрузки (при масштабировании)

**Конфигурация:**
```nginx
location /soa/api/v1/ {
    proxy_pass https://called-web:8443/soa/api/v1/;
    proxy_ssl_verify off;
}

location /orgmanager/api/v1/ {
    proxy_pass http://zuul-gateway:8080/orgmanager/api/v1/;
}
```

---

### 2. **Zuul Gateway (Netflix Zuul)**

**Роль:** API Gateway для маршрутизации запросов через Eureka Service Discovery.

**Технологии:**
- Spring Boot 2.3.12
- Spring Cloud Hoxton.SR12
- Netflix Zuul 1.x
- Netflix Ribbon (client-side load balancing)
- Netflix Eureka Client

**Функции:**
- **Динамическая маршрутизация:** Получает список сервисов из Eureka и направляет запросы
- **Load Balancing:** Ribbon балансирует нагрузку между инстансами caller-service
- **Service Discovery Integration:** Автоматически обнаруживает новые экземпляры сервисов
- **Routing Rules:**
  ```yaml
  zuul:
    routes:
      orgmanager:
        path: /orgmanager/**
        service-id: caller-service
  ```

**Сетевое взаимодействие:**
```
Client → Zuul (8080) → Eureka (lookup: caller-service) → Ribbon → caller-service (8080)
```

**Конфигурация (application.yml):**
```yaml
zuul:
  routes:
    orgmanager:
      path: /orgmanager/**
      service-id: caller-service
      strip-prefix: false

ribbon:
  eureka:
    enabled: true
  ConnectTimeout: 5000
  ReadTimeout: 60000
```

---

### 3. **Caller Service (Spring Boot)**

**Роль:** Микросервис для дополнительных операций (увольнение сотрудников, поглощение организаций).

**Технологии:**
- Spring Boot 2.3.12.RELEASE
- Spring Cloud Config Client
- Netflix Eureka Client
- Netflix Ribbon
- Spring Web (REST)
- Spring Cloud Context (`@RefreshScope`)

**Функции:**
- **REST API Endpoints:**
  - `POST /api/v1/fire/all/{id}` - Увольнение всех сотрудников организации
  - `POST /api/v1/acquire/{acquirer-id}/{acquired-id}` - Поглощение организации
- **Service Discovery:** Регистрируется в Eureka как `caller-service`
- **Config Management:** Получает конфигурацию из Config Server через bootstrap.yml
- **Dynamic Refresh:** Поддерживает обновление конфигурации через `/actuator/refresh`
- **REST Client:** Вызывает called-web через HTTPS

**Сетевое взаимодействие:**
```
1. Startup:
   caller-service → Config Server (8888) → получение конфигурации
   caller-service → Eureka (8761) → регистрация как "caller-service"

2. Request:
   Zuul → caller-service → HTTPS → called-web:8443 → Consul → called-ejb
```

**Bootstrap конфигурация (bootstrap.yml):**
```yaml
spring:
  application:
    name: caller-service
  cloud:
    config:
      uri: http://config-server:8888
      fail-fast: false

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
```

**Dynamic Configuration Example:**
```java
@RestController
@RefreshScope  // Позволяет обновлять @Value без перезапуска
public class ConfigTestController {
    @Value("${app.message}")
    private String message;
    
    // POST /actuator/refresh обновит значение message
}
```

---

### 4. **Config Server (Spring Cloud Config)**

**Роль:** Централизованное управление конфигурацией для всех Spring Boot сервисов.

**Технологии:**
- Spring Cloud Config Server 2.3.12
- Spring Boot Actuator
- File-based configuration (classpath:/config/)

**Функции:**
- **Configuration Distribution:**
  - Хранит конфигурации в `src/main/resources/config/{service-name}.yml`
  - Предоставляет REST API для получения конфигурации: `GET /{application}/{profile}`
  - Поддерживает профили (default, dev, prod)
  
- **Dynamic Refresh:**
  - Клиенты с `@RefreshScope` могут обновлять конфигурацию без перезапуска
  - Триггер: `POST /actuator/refresh` на клиенте

- **High Availability:**
  - Регистрируется в Eureka для обнаружения другими сервисами
  - Поддерживает fallback на локальную конфигурацию при недоступности

**Архитектура работы:**
```
1. Startup:
   Config Server → Загрузка конфигураций из classpath:/config/
   Config Server → Eureka (регистрация)

2. Client Bootstrap:
   caller-service (bootstrap.yml) → Config Server (http://config-server:8888)
   Config Server → Возврат caller-service.yml
   caller-service → Применение конфигурации

3. Refresh:
   Admin → POST /actuator/refresh (caller-service)
   caller-service → Config Server (повторный запрос конфигурации)
   @RefreshScope beans → Перезагрузка с новыми значениями
```

**Пример конфигурации (config/caller-service.yml):**
```yaml
server:
  port: 8080

app:
  message: Hello from Config Server v2

called-service:
  url: https://called-web:8443/soa/api/v1

eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

**Преимущества:**
- Единая точка управления конфигурацией
- Изменение настроек без пересборки Docker образов
- Разделение конфигурации по окружениям
- Аудит изменений (при использовании Git backend)

---

### 5. **Eureka Server (Netflix Eureka)**

**Роль:** Service Registry для Spring Cloud микросервисов.

**Технологии:**
- Spring Cloud Netflix Eureka Server 2.3.12
- REST API для регистрации/обнаружения сервисов
- Heartbeat механизм

**Функции:**
- **Service Registration:**
  - Сервисы регистрируются при старте: `POST /eureka/apps/{APP_NAME}`
  - Отправляют heartbeat каждые 30 секунд
  - Автоматически удаляются при отсутствии heartbeat (90 секунд по умолчанию)

- **Service Discovery:**
  - Предоставляет список доступных инстансов: `GET /eureka/apps`
  - Ribbon использует эту информацию для балансировки нагрузки

- **Health Monitoring:**
  - Отслеживает состояние сервисов через heartbeat
  - Удаляет недоступные инстансы из реестра

**Зарегистрированные сервисы:**
```
CALLER-SERVICE:
  - Instance: 172.18.0.9:8080
  - Status: UP
  - Zone: defaultZone

ZUUL-GATEWAY:
  - Instance: 172.18.0.X:8080
  - Status: UP
  - Zone: defaultZone

CONFIG-SERVER:
  - Instance: 172.18.0.X:8888
  - Status: UP
  - Zone: defaultZone
```

**API Endpoints:**
- `GET /eureka/apps` - Список всех сервисов
- `POST /eureka/apps/{APP_ID}` - Регистрация сервиса
- `PUT /eureka/apps/{APP_ID}/{INSTANCE_ID}` - Heartbeat
- `DELETE /eureka/apps/{APP_ID}/{INSTANCE_ID}` - Отмена регистрации

**Ribbon Integration:**
```java
// Zuul автоматически использует Ribbon для балансировки
@Bean
public IRule ribbonRule() {
    return new RoundRobinRule();  // или ZoneAvoidanceRule (default)
}
```

---

### 6. **Consul**

**Роль:** Service Discovery для WildFly сервисов (called-ejb, called-web).

**Технологии:**
- HashiCorp Consul 1.15
- HTTP API для service discovery
- Health checking
- Key/Value store

**Функции:**
- **Service Registration:**
  - called-ejb регистрируется в Consul при старте через HTTP PUT
  - Отправляет health checks периодически

- **Service Discovery:**
  - called-web запрашивает список called-ejb инстансов
  - `GET /v1/health/service/called-ejb?passing=true`
  - Возвращает список здоровых инстансов с IP:PORT

- **Health Checking:**
  - HTTP health checks на `/health` endpoint
  - TTL-based checks
  - Удаляет нездоровые инстансы из реестра

**Сетевое взаимодействие called-web → called-ejb:**
```java
// 1. ConsulServiceDiscovery.java
String consulUrl = "http://consul:8500/v1/health/service/called-ejb?passing=true";
Response response = httpClient.newCall(request).execute();
// Ответ: [{"Service": {"Address": "172.18.0.6", "Port": 8080}}]

// 2. EjbLookup.java
String serviceAddress = "172.18.0.6:8080";
Properties props = new Properties();
props.put(Context.PROVIDER_URL, "http-remoting://" + serviceAddress);

InitialContext context = new InitialContext(props);
OrganizationServiceRemote service = (OrganizationServiceRemote) 
    context.lookup("ejb:/called-ejb/OrganizationServiceBean!itmo.ivank.ejb.service.OrganizationServiceRemote");
```

**Consul UI:** http://localhost:8500/ui

---

### 7. **Called-Web (JAX-RS / WildFly)**

**Роль:** REST API фасад для бизнес-логики в called-ejb.

**Технологии:**
- WildFly 39.0.0
- Jakarta EE 10 (JAX-RS 3.1)
- JBoss EJB Client 5.0.8
- Consul Client (HTTP)
- HTTPS/TLS

**Функции:**
- **REST API Endpoints:**
  - `GET /soa/api/v1/organizations` - Список организаций с пагинацией
  - `POST /soa/api/v1/organizations` - Создание организации
  - `GET /soa/api/v1/organizations/{id}` - Получение организации
  - `PUT /soa/api/v1/organizations/{id}` - Обновление организации
  - `DELETE /soa/api/v1/organizations/{id}` - Удаление организации
  - И аналогично для employees...

- **Service Discovery Integration:**
  - При каждом запросе опрашивает Consul для получения адреса called-ejb
  - Кеширует EJB proxy для переиспользования
  - Обрабатывает отказы и переподключения

- **EJB Remote Invocation:**
  - Использует JBoss Remoting для вызова EJB на called-ejb
  - HTTP Remoting протокол (не RMI)
  - JNDI lookup через InitialContext

**Поток запроса:**
```
1. REST Request:
   Client → NGINX → called-web:8443/soa/api/v1/organizations

2. Service Discovery:
   OrganizationResource → ConsulServiceDiscovery
   → HTTP GET http://consul:8500/v1/health/service/called-ejb?passing=true
   → Response: [{"Service": {"Address": "172.18.0.6", "Port": 8080}}]

3. EJB Lookup:
   EjbLookup → InitialContext (provider: http-remoting://172.18.0.6:8080)
   → JNDI Lookup: ejb:/called-ejb/OrganizationServiceBean!...OrganizationServiceRemote
   → EJB Proxy получен

4. Business Logic:
   OrganizationResource → organizationService.getAllOrganizations(page, size)
   → HTTP Remoting → called-ejb:8080 → OrganizationServiceBean
   → JPA Query → PostgreSQL
   → Response ← ← ←

5. XML Serialization:
   JAX-RS → JAXB → XML Response
```

**Consul Discovery Example:**
```java
public String discoverService(String serviceName) {
    String url = String.format("http://%s:%d/v1/health/service/%s?passing=true", 
                                consulHost, consulPort, serviceName);
    
    // HTTP запрос к Consul
    JSONArray services = new JSONArray(responseBody);
    JSONObject firstService = services.getJSONObject(0).getJSONObject("Service");
    
    String address = firstService.getString("Address");
    int port = firstService.getInt("Port");
    
    return address + ":" + port;
}
```

---

### 8. **Called-EJB (Enterprise JavaBeans / WildFly)**

**Роль:** Бизнес-логика и работа с базой данных через JPA.

**Технологии:**
- WildFly 39.0.0
- Jakarta EE 10 (EJB 4.0, JPA 3.1)
- Hibernate 6.2 (JPA Implementation)
- PostgreSQL JDBC Driver
- Consul Registration

**Функции:**
- **Business Logic (Session Beans):**
  - `@Stateless OrganizationServiceBean` - CRUD операции с организациями
  - `@Stateless EmployeeServiceBean` - CRUD операции с сотрудниками
  - Remote интерфейсы для вызова из called-web

- **Data Access Layer:**
  - JPA Entity Manager для работы с БД
  - Criteria API для динамических запросов
  - Транзакционное управление через `@TransactionAttribute`

- **Service Registration:**
  - Регистрируется в Consul при старте
  - Health check endpoint для Consul

**EJB Remote Interface Example:**
```java
@Remote
public interface OrganizationServiceRemote {
    OrganizationsPage getAllOrganizations(int page, int size);
    Organization getOrganizationById(Long id);
    Organization createOrganization(OrganizationRequest request);
    Organization updateOrganization(Long id, OrganizationRequest request);
    void deleteOrganization(Long id);
}

@Stateless
public class OrganizationServiceBean implements OrganizationServiceRemote {
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public OrganizationsPage getAllOrganizations(int page, int size) {
        CriteriaQuery<Organization> query = ...
        List<Organization> results = em.createQuery(query)
            .setFirstResult((page - 1) * size)
            .setMaxResults(size)
            .getResultList();
        return new OrganizationsPage(results, page, size, totalElements);
    }
}
```

**JPA Configuration (persistence.xml):**
```xml
<persistence-unit name="soa-pu">
    <jta-data-source>java:jboss/datasources/PostgresDS</jta-data-source>
    <properties>
        <property name="hibernate.dialect" value="org.hibernate.dialect.PostgreSQLDialect"/>
        <property name="hibernate.hbm2ddl.auto" value="update"/>
        <property name="hibernate.show_sql" value="true"/>
    </properties>
</persistence-unit>
```

**Database Schema:**
```sql
CREATE TABLE organizations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    creation_date DATE,
    annual_turnover FLOAT,
    full_name TEXT,
    type VARCHAR(50),
    coordinates_x INTEGER,
    coordinates_y FLOAT,
    official_address_street VARCHAR(255),
    official_address_town_name VARCHAR(255),
    official_address_town_x INTEGER,
    official_address_town_y DOUBLE PRECISION
);

CREATE TABLE employees (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    salary FLOAT,
    organization_id BIGINT REFERENCES organizations(id) ON DELETE CASCADE
);
```

---

### 9. **PostgreSQL**

**Роль:** Реляционная база данных для хранения организаций и сотрудников.

**Технологии:**
- PostgreSQL 16
- JDBC Driver
- Connection Pooling (WildFly DataSource)

**Конфигурация:**
```yaml
postgres:
  environment:
    POSTGRES_DB: postgres
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: postgres
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U postgres"]
```

**WildFly DataSource (standalone.xml):**
```xml
<datasource jndi-name="java:jboss/datasources/PostgresDS" pool-name="PostgresDS">
    <connection-url>jdbc:postgresql://postgres:5432/postgres</connection-url>
    <driver>postgresql</driver>
    <security>
        <user-name>postgres</user-name>
        <password>postgres</password>
    </security>
</datasource>
```

---

## Потоки данных

### Поток 1: CRUD операция через called-web

```
┌─────────┐     HTTPS      ┌──────────┐    Consul     ┌────────────┐   HTTP      ┌────────────┐   JDBC    ┌──────────┐
│  Client │───────────────►│   NGINX  │──────────────►│ called-web │────────────►│ called-ejb │──────────►│ postgres │
└─────────┘  /soa/api/v1   └──────────┘   Discovery   └────────────┘  Remoting   └────────────┘   Query   └──────────┘
                                              │                            │
                                              ▼                            ▼
                                         ┌────────┐                  ┌─────────┐
                                         │ Consul │                  │ EJB     │
                                         │  :8500 │                  │ Proxy   │
                                         └────────┘                  └─────────┘

Шаги:
1. Client → NGINX (https://localhost:8448/soa/api/v1/organizations)
2. NGINX → called-web:8443 (TLS proxy)
3. called-web → Consul API (GET /v1/health/service/called-ejb?passing=true)
4. Consul → called-web (Response: [{"Service": {"Address": "172.18.0.6", "Port": 8080}}])
5. called-web → JNDI Lookup (ejb:/called-ejb/OrganizationServiceBean!...)
6. called-web → called-ejb:8080 (HTTP Remoting protocol)
7. called-ejb → PostgreSQL (JPA/Hibernate query)
8. PostgreSQL → called-ejb (Result set)
9. called-ejb → called-web (Serialized response)
10. called-web → NGINX → Client (XML response)
```

### Поток 2: Дополнительная операция через Zuul Gateway

```
┌─────────┐     HTTPS      ┌──────────┐      HTTP       ┌──────────┐    Eureka    ┌────────────────┐
│  Client │───────────────►│   NGINX  │────────────────►│   Zuul   │─────────────►│ caller-service │
└─────────┘ /orgmanager    └──────────┘                 └──────────┘  Discovery   └────────┬───────┘
                                                              │                            │
                                                              ▼                            │ HTTPS
                                                         ┌────────┐                        │
                                                         │ Eureka │                        │
                                                         │  :8761 │                        │
                                                         └────────┘                        ▼
                                                                                    ┌────────────┐
                                                                                    │ called-web │
                                                                                    └──────┬─────┘
                                                                                           │
                                                                                    (Consul Discovery)
                                                                                           │
                                                                                           ▼
                                                                                    ┌────────────┐
                                                                                    │ called-ejb │
                                                                                    └──────┬─────┘
                                                                                           │
                                                                                           ▼
                                                                                    ┌──────────┐
                                                                                    │ postgres │
                                                                                    └──────────┘

Шаги:
1. Client → NGINX (https://localhost:8448/orgmanager/api/v1/fire/all/1)
2. NGINX → Zuul:8080 (HTTP proxy)
3. Zuul → Eureka (GET /eureka/apps/CALLER-SERVICE)
4. Eureka → Zuul (Instance list: [172.18.0.9:8080])
5. Ribbon → Load Balancing Decision (Round Robin)
6. Zuul → caller-service:8080 (HTTP)
7. caller-service → called-web:8443 (HTTPS REST call)
8. called-web → Consul → called-ejb (как в Потоке 1)
9. called-ejb → PostgreSQL (DELETE FROM employees WHERE organization_id = 1)
10. Response ← ← ← ← ← ← (XML response с количеством удалённых записей)
```

### Поток 3: Получение конфигурации

```
┌────────────────┐   Bootstrap   ┌───────────────┐   File Read   ┌──────────────────────────┐
│ caller-service │──────────────►│ Config Server │──────────────►│ classpath:/config/       │
│  (startup)     │               │    :8888      │               │ caller-service.yml       │
└────────────────┘               └───────────────┘               └──────────────────────────┘
       │                                 │
       │ Spring Cloud Config             │ REST API
       │ Client                          │ GET /{application}/{profile}
       │                                 │
       ▼                                 ▼
┌────────────────────────────────────────────────────────────┐
│  HTTP Request:                                             │
│  GET http://config-server:8888/caller-service/default     │
│                                                            │
│  Response (JSON):                                          │
│  {                                                         │
│    "name": "caller-service",                               │
│    "profiles": ["default"],                                │
│    "propertySources": [{                                   │
│      "name": "classpath:/config/caller-service.yml",       │
│      "source": {                                           │
│        "server.port": 8080,                                │
│        "app.message": "Hello from Config Server",          │
│        "called-service.url": "https://called-web:8443/..." │
│      }                                                     │
│    }]                                                      │
│  }                                                         │
└────────────────────────────────────────────────────────────┘

Шаги динамического обновления:
1. Admin → Изменяет caller-service.yml в Config Server
2. Admin → Пересобирает config-server (docker compose build config-server)
3. Admin → Перезапускает config-server (docker compose up -d config-server)
4. Admin → POST http://localhost:8445/orgmanager/actuator/refresh
5. caller-service → Config Server (повторный запрос конфигурации)
6. Config Server → caller-service (обновлённые значения)
7. @RefreshScope beans → Перезагрузка (без перезапуска контейнера!)
8. caller-service → Новые значения применены
```

---

## Зависимости и порядок запуска

### Граф зависимостей

```
                    postgres (healthcheck: pg_isready)
                         │
    consul ──────────────┼─────────────────────────────────┐
(healthcheck: /status)   │                                 │
         │               ▼                                 │
         │         called-ejb                              │
         │      (healthcheck: /health)                     │
         │               │                                 │
         └──────────────►│                                 │
                         ▼                                 │
                    called-web ◄────────────────────────────┘
                (healthcheck: /health)
                         │
                         │
eureka-server ───────────┼───────────► config-server
(healthcheck)            │             (healthcheck)
         │               │                   │
         │               │                   │
         └───────────────┴───────────────────┘
                         │
                         ▼
                  caller-service
              (healthcheck: /actuator/health)
                         │
                         ▼
                   zuul-gateway
              (healthcheck: /actuator/health)
                         │
                         ▼
                     frontend
```

### Порядок запуска (Docker Compose)

```bash
# 1. База данных и Service Discovery
docker compose up -d postgres consul eureka-server

# 2. Config Server (после Eureka)
docker compose up -d config-server

# 3. WildFly сервисы (после postgres + consul)
docker compose up -d called-ejb called-web

# 4. Spring сервисы (после eureka + config + called-web)
docker compose up -d caller-service

# 5. API Gateway (после caller-service)
docker compose up -d zuul-gateway

# 6. Frontend (после zuul + called-web)
docker compose up -d frontend

# Или всё сразу с автоматическим ожиданием:
docker compose up -d
# Docker Compose автоматически учитывает depends_on с condition: service_healthy
```

---

## Health Checks

### Конфигурация

| Сервис | Endpoint | Интервал | Start Period | Назначение |
|--------|----------|----------|--------------|------------|
| postgres | `pg_isready -U postgres` | 10s | 10s | Проверка готовности БД |
| consul | `http://localhost:8500/v1/status/leader` | 10s | 10s | Проверка Consul кластера |
| eureka-server | `http://localhost:8761/actuator/health` | 10s | 30s | Spring Actuator health |
| config-server | `http://localhost:8888/actuator/health` | 10s | 30s | Spring Actuator health |
| called-ejb | `http://localhost:9990/health` | 10s | 60s | WildFly management API |
| called-web | `http://localhost:9990/health` | 10s | 60s | WildFly management API |
| caller-service | `http://localhost:8080/orgmanager/actuator/health` | 10s | 30s | Spring Actuator health |
| zuul-gateway | `http://localhost:8080/actuator/health` | 10s | 30s | Spring Actuator health |

### Преимущества

- **Автоматическое управление зависимостями:** Docker Compose не запустит зависимый сервис, пока healthcheck не пройдёт
- **Предотвращение race conditions:** Config Server гарантированно доступен до запуска caller-service
- **Раннее обнаружение проблем:** Сервис помечается как unhealthy при сбоях
- **Автоматический restart:** Docker может перезапускать сервисы при падении healthcheck

---

## API Endpoints

### Called-Web (CRUD Operations)

**Base URL:** `https://localhost:8448/soa/api/v1`

#### Organizations
```bash
# Получить список организаций
GET /organizations?page=1&size=20

# Создать организацию
POST /organizations
Content-Type: application/xml
<organizationRequest>
  <name>Company Ltd</name>
  <annualTurnover>1000000</annualTurnover>
  <type>COMMERCIAL</type>
</organizationRequest>

# Получить организацию по ID
GET /organizations/{id}

# Обновить организацию
PUT /organizations/{id}

# Удалить организацию
DELETE /organizations/{id}

# Получить сотрудников организации
GET /organizations/{id}/employees

# Агрегатные операции
GET /organizations/turnover
GET /organizations/types
```

#### Employees
```bash
# Создать сотрудника
POST /employees

# Получить сотрудника
GET /employees/{id}

# Обновить сотрудника
PUT /employees/{id}

# Удалить сотрудника
DELETE /employees/{id}

# Batch операции
POST /employees/batch/create
POST /employees/batch/update
POST /employees/batch/delete
```

### Caller-Service (Additional Operations)

**Base URL:** `https://localhost:8448/orgmanager/api/v1`

```bash
# Уволить всех сотрудников организации
POST /fire/all/{id}
Response: <employeeCount>5</employeeCount>

# Поглощение организации
POST /acquire/{acquirer-id}/{acquired-id}
Response: <acquiring>
  <acquirerOrganization>...</acquirerOrganization>
  <acquiredOrganization>...</acquiredOrganization>
  <numberOfEmployeesMoved>10</numberOfEmployeesMoved>
</acquiring>

# Тестовый endpoint для конфигурации
GET /config-test
Response: <config>
  <message>Hello from Config Server</message>
  <calledServiceUrl>https://called-web:8443/soa/api/v1</calledServiceUrl>
</config>
```

### Actuator Endpoints

```bash
# Health check
GET http://localhost:8445/orgmanager/actuator/health
GET http://localhost:8090/actuator/health
GET http://localhost:8888/actuator/health

# Обновить конфигурацию (только caller-service)
POST http://localhost:8445/orgmanager/actuator/refresh
Response: <KeySet><item>app.message</item></KeySet>

# Info endpoint
GET http://localhost:8445/orgmanager/actuator/info
```

### Service Discovery Endpoints

```bash
# Eureka - список всех сервисов
GET http://localhost:8761/eureka/apps

# Consul - список called-ejb инстансов
GET http://localhost:8500/v1/health/service/called-ejb?passing=true

# Consul UI
http://localhost:8500/ui

# Eureka UI
http://localhost:8761/
```

---

## Технологический стек

### Backend Services

| Компонент | Технологии |
|-----------|------------|
| **called-ejb** | Java 17, Jakarta EE 10 (EJB 4.0, JPA 3.1), Hibernate 6.2, WildFly 39, PostgreSQL JDBC |
| **called-web** | Java 17, Jakarta EE 10 (JAX-RS 3.1), WildFly 39, JBoss EJB Client 5.0.8 |
| **caller-service** | Java 11, Spring Boot 2.3.12, Spring Cloud Hoxton.SR12, Netflix Ribbon |
| **zuul-gateway** | Java 11, Spring Boot 2.3.12, Netflix Zuul 1.x, Netflix Ribbon, Eureka Client |
| **config-server** | Java 11, Spring Boot 2.3.12, Spring Cloud Config Server |
| **eureka-server** | Java 11, Spring Boot 2.3.12, Netflix Eureka Server |

### Infrastructure

| Компонент | Версия | Назначение |
|-----------|--------|------------|
| **PostgreSQL** | 16 | Реляционная БД |
| **Consul** | 1.15 | Service Discovery (WildFly) |
| **NGINX** | 1.25 | Reverse Proxy, SSL Termination |
| **Docker** | 24.x | Контейнеризация |
| **Docker Compose** | 2.x | Оркестрация контейнеров |

### Frontend

| Технология | Версия |
|------------|--------|
| React | 18.x |
| TypeScript | 5.x |
| Vite | 5.x |
| Axios | 1.x |

---

## Запуск проекта

### Предварительные требования

- Docker 24.x+
- Docker Compose 2.x+
- 8 GB RAM
- Свободные порты: 443, 5432, 8080, 8090, 8444-8448, 8500, 8761, 8888, 9990, 9992

### Генерация SSL сертификатов

```powershell
# Windows
.\generate-certs.ps1

# Linux/macOS
chmod +x generate-certs.sh
./generate-certs.sh
```

### Сборка и запуск

```bash
# Сборка всех образов
docker compose build

# Запуск всех сервисов
docker compose up -d

# Просмотр логов
docker compose logs -f

# Просмотр логов конкретного сервиса
docker compose logs -f caller-service

# Проверка статуса
docker compose ps

# Остановка
docker compose down

# Полная очистка (включая volumes)
docker compose down -v
```

### Проверка работоспособности

```bash
# Frontend
curl -k https://localhost:8448/

# CRUD операции
curl -k https://localhost:8448/soa/api/v1/organizations

# Дополнительные операции
curl -k -X POST https://localhost:8448/orgmanager/api/v1/fire/all/1

# Eureka Dashboard
http://localhost:8761/

# Consul UI
http://localhost:8500/ui

# Health checks
curl http://localhost:8445/orgmanager/actuator/health
curl http://localhost:8090/actuator/health
```

---

## Динамическое обновление конфигурации

### Пример обновления без перезапуска сервиса

```bash
# 1. Изменить конфигурацию
vim config-server/src/main/resources/config/caller-service.yml
# Изменить: app.message: "New message v3"

# 2. Пересобрать только config-server
docker compose build config-server

# 3. Перезапустить только config-server
docker compose up -d config-server

# 4. Подождать 15-20 секунд

# 5. Проверить текущее значение (старое)
curl http://localhost:8445/orgmanager/api/v1/config-test
# <config><message>Old message v2</message>...</config>

# 6. Обновить конфигурацию в caller-service (БЕЗ ПЕРЕЗАПУСКА!)
curl -X POST http://localhost:8445/orgmanager/actuator/refresh
# <KeySet><item>app.message</item></KeySet>

# 7. Проверить новое значение
curl http://localhost:8445/orgmanager/api/v1/config-test
# <config><message>New message v3</message>...</config>
```

**Преимущества:**
- ✅ Нет даунтайма
- ✅ Не нужно пересобирать образы сервисов
- ✅ Изменения применяются за секунды
- ✅ Можно откатить изменения

---

## Мониторинг и отладка

### Полезные команды

```bash
# Проверить регистрацию в Eureka
curl http://localhost:8761/eureka/apps | grep -A 5 CALLER-SERVICE

# Проверить регистрацию в Consul
curl http://localhost:8500/v1/health/service/called-ejb?passing=true

# Проверить логи service discovery в called-web
docker logs soa-called-web 2>&1 | grep -i consul

# Проверить Zuul маршрутизацию
docker logs soa-zuul-gateway 2>&1 | grep -i "DynamicServerListLoadBalancer"

# Проверить конфигурацию caller-service
docker logs soa-caller-service 2>&1 | grep -i "Located property"

# WildFly Management Console
http://localhost:9990/  # called-web
http://localhost:9992/  # called-ejb
```

### Диагностика проблем

**Проблема:** caller-service не может подключиться к config-server

**Решение:**
```bash
# Проверить healthcheck config-server
docker inspect soa-config-server | grep -A 10 Health

# Проверить порядок запуска
docker compose ps

# Перезапустить caller-service после config-server
docker compose restart caller-service
```

**Проблема:** called-web не может найти called-ejb через Consul

**Решение:**
```bash
# Проверить регистрацию в Consul
curl http://localhost:8500/v1/catalog/services

# Проверить health check
curl http://localhost:8500/v1/health/service/called-ejb

# Проверить логи
docker logs soa-called-web | grep -i "discovered\|consul"
```

---

## Масштабирование

### Горизонтальное масштабирование

```bash
# Масштабировать caller-service до 3 инстансов
docker compose up -d --scale caller-service=3

# Zuul + Ribbon автоматически распределят нагрузку между инстансами
# Ribbon использует Round Robin по умолчанию

# Проверить инстансы в Eureka
curl http://localhost:8761/eureka/apps/CALLER-SERVICE
```

**Автоматическая балансировка нагрузки:**
- Zuul получает список инстансов из Eureka
- Ribbon выбирает инстанс по алгоритму (Round Robin / Zone Avoidance)
- Каждый запрос идёт на разный инстанс
- При падении инстанса - автоматическое исключение из балансировки

---

## Production Considerations

### Security

- ✅ TLS шифрование (NGINX → called-web)
- ✅ TLS шифрование (caller-service → called-web)
- ⚠️ Сертификаты self-signed (требуется заменить на CA-signed)
- ⚠️ Нет аутентификации/авторизации (требуется добавить OAuth2/JWT)
- ⚠️ Database credentials в environment variables (использовать secrets)

### Observability

**Требуется добавить:**
- Distributed Tracing (Zipkin / Jaeger)
- Metrics (Prometheus + Grafana)
- Centralized Logging (ELK Stack / Loki)
- APM (Application Performance Monitoring)

**Текущий мониторинг:**
- Health checks через Actuator
- Docker logs
- Eureka Dashboard
- Consul UI

### High Availability

**Текущее состояние:**
- ✅ Поддержка масштабирования caller-service
- ✅ Service Discovery для автоматического обнаружения
- ⚠️ Single point of failure: postgres, consul, eureka
- ⚠️ Нет persistence для Eureka registry

**Рекомендации:**
- Запустить Eureka в peer-to-peer режиме (3+ инстанса)
- Consul cluster (3+ серверные ноды)
- PostgreSQL replication (Primary + Replica)
- Config Server backed by Git repository

---

## Лицензия

MIT

---

## Контакты

ITMO University - SOA Lab Course

**Технологии:**
- Spring Cloud (Eureka, Config, Zuul, Ribbon)
- Jakarta EE (JAX-RS, EJB, JPA)
- HashiCorp Consul
- WildFly Application Server
- PostgreSQL
- Docker & Docker Compose
