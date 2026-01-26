# SOA Lab Course - Service-Oriented Architecture

## Обзор архитектуры

Проект представляет собой распределённую систему управления организациями и сотрудниками, построенную на принципах сервис-ориентированной архитектуры (SOA). Система включает множество компонентов, взаимодействующих через различные протоколы (REST, SOAP, HTTP) с использованием Enterprise Service Bus (Mule ESB) для интеграции.

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                    КЛИЕНТСКИЙ УРОВЕНЬ                                   │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │                          Frontend (Nginx + React)                               │   │
│  │                              https://localhost:8448                             │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
│                    │                                        │                           │
│         /soa/api/v1/*                            /orgmanager/api/v1/*                   │
│                    ▼                                        ▼                           │
├────────────────────┼────────────────────────────────────────┼───────────────────────────┤
│                    │           API GATEWAY УРОВЕНЬ          │                           │
│                    │                                        │                           │
│                    │                    ┌───────────────────┴──────────────────┐        │
│                    │                    │         Zuul Gateway                 │        │
│                    │                    │         (Spring Cloud)               │        │
│                    │                    │         :8090 (8080 internal)        │        │
│                    │                    │   Ribbon LB + Eureka Discovery       │        │
│                    │                    └───────────────────┬──────────────────┘        │
│                    │                                        │                           │
├────────────────────┼────────────────────────────────────────┼───────────────────────────┤
│                    │          SERVICE MESH УРОВЕНЬ          │                           │
│                    │                                        ▼                           │
│                    │                    ┌───────────────────────────────────────┐       │
│                    │                    │          REST Adapter                 │       │
│                    │                    │        (Spring Boot 2.3.12)           │       │
│                    │                    │     :8445 (8080 internal)             │       │
│                    │                    │   REST → SOAP Protocol Bridge        │       │
│                    │                    └───────────────────┬───────────────────┘       │
│                    │                                        │ SOAP/HTTP                 │
│                    │                                        ▼                           │
│                    │                    ┌───────────────────────────────────────┐       │
│                    │                    │          SOAP Caller                  │       │
│                    │                    │    (WildFly 26.1.3 + JAX-WS)          │       │
│                    │                    │          :8083 (8080)                 │       │
│                    │                    │   SOAP Web Service Endpoint           │       │
│                    │                    └───────────────────┬───────────────────┘       │
│                    │                                        │ HTTP/XML                  │
│                    │                                        ▼                           │
├────────────────────┼────────────────────────────────────────┼───────────────────────────┤
│                    │          INTEGRATION BUS УРОВЕНЬ       │                           │
│                    │                                        │                           │
│                    │                    ┌───────────────────┴───────────────────┐       │
│                    │                    │            Mule ESB 4.10.2            │       │
│                    │                    │     Enterprise Service Bus            │       │
│                    │                    │              :8082                     │       │
│                    │                    │   Protocol Mediation & Routing        │       │
│                    │                    └───────────────────┬───────────────────┘       │
│                    │                                        │ HTTPS                     │
│                    ▼                                        ▼                           │
├─────────────────────────────────────────────────────────────────────────────────────────┤
│                              BACKEND SERVICE УРОВЕНЬ                                    │
│                                                                                         │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │                          Called-Web (WildFly 35)                                │   │
│  │                       REST API + EJB Client Proxy                               │   │
│  │                    :8444 (8443 HTTPS internal)                                  │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
│                                          │ EJB/RMI over HTTPS                           │
│                                          ▼                                              │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │                          Called-EJB (WildFly 35)                                │   │
│  │                       Business Logic + JPA Repository                           │   │
│  │                           :8446 (8443 HTTPS)                                    │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
│                                          │ JDBC                                         │
│                                          ▼                                              │
│  ┌─────────────────────────────────────────────────────────────────────────────────┐   │
│  │                          PostgreSQL Database                                    │   │
│  │                               :5432                                             │   │
│  └─────────────────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────────────────┘

                              SERVICE DISCOVERY & CONFIG
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────────────┐  │
│  │   Eureka Server      │  │   Config Server      │  │        Consul               │  │
│  │   :8761              │  │   :8888              │  │        :8500                │  │
│  │   Spring Cloud       │  │   Spring Cloud       │  │   WildFly Discovery         │  │
│  │   Services Registry  │  │   Centralized Config │  │   (called-ejb/called-web)   │  │
│  └──────────────────────┘  └──────────────────────┘  └──────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## Компоненты системы

### 1. Frontend (Nginx + React/TypeScript)

**Роль:** Точка входа пользователя, статический веб-сервер и реверс-прокси.

| Параметр | Значение |
|----------|----------|
| Технологии | Nginx 1.29, React 19, TypeScript, Vite |
| Внешний порт | 8448 (HTTPS) |
| Внутренний порт | 443 (HTTPS) |

**Функции:**
- Раздача статических файлов React-приложения
- TLS терминация для внешних клиентов
- Маршрутизация API запросов к backend-сервисам

**Конфигурация маршрутизации (nginx):**

```nginx
# Прямой прокси к Called-Web (основной REST API)
location /soa/api/v1/ {
    proxy_pass https://called-web:8443/soa/api/v1/;
    proxy_ssl_verify off;
}

# Прокси через Zuul Gateway (orgmanager API)
location /orgmanager/api/v1/ {
    proxy_pass http://zuul-gateway:8080/orgmanager/api/v1/;
}
```

---

### 2. Zuul Gateway (Spring Cloud Netflix Zuul)

**Роль:** API Gateway с динамической маршрутизацией и балансировкой нагрузки.

| Параметр | Значение |
|----------|----------|
| Технологии | Spring Boot 2.3.12, Netflix Zuul 1.3, Ribbon, Hystrix |
| Внешний порт | 8090 |
| Внутренний порт | 8080 |

**Функции:**
- Динамическая маршрутизация на основе Eureka Service Discovery
- Client-side load balancing через Netflix Ribbon
- Circuit Breaker через Hystrix

**Конфигурация маршрутов:**

```yaml
zuul:
  routes:
    orgmanager:
      path: /orgmanager/**
      serviceId: rest-adapter    # Резолвится через Eureka
      stripPrefix: false
```

**Механизм работы:**
1. Получает запрос на `/orgmanager/api/v1/*`
2. Запрашивает у Eureka список инстансов `rest-adapter`
3. Ribbon выбирает инстанс по алгоритму Round Robin
4. Проксирует запрос с Hystrix fallback

---

### 3. REST Adapter (Spring Boot)

**Роль:** Протокольный мост REST → SOAP для интеграции с legacy SOAP-сервисом.

| Параметр | Значение |
|----------|----------|
| Технологии | Spring Boot 2.3.12, SAAJ (SOAP API), Spring Cloud Config |
| Внешний порт | 8445 |
| Внутренний порт | 8080 |
| Eureka Service ID | `rest-adapter` |

**Функции:**
- Преобразование REST-запросов в SOAP-сообщения
- Парсинг SOAP-ответов и преобразование в REST (XML/JSON)
- Интеграция с Spring Cloud Config для динамической конфигурации
- Propagation HTTP статус-кодов из SOAP Fault

**API Endpoints:**

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/orgmanager/api/v1/fire/all/{id}` | Уволить всех сотрудников организации |
| POST | `/orgmanager/api/v1/acquire/{acquirerId}/{acquiredId}` | Поглотить организацию |

**Формирование SOAP-запроса (пример):**

```java
// Создание SOAP Envelope
SOAPEnvelope envelope = soapPart.getEnvelope();
envelope.addNamespaceDeclaration("cal", "http://soap.ivank.itmo/caller");

// Добавление операции в Body
SOAPBody body = envelope.getBody();
SOAPElement operation = body.addChildElement("fireAllOrgEmployees", "cal");

// Параметры БЕЗ namespace prefix (elementFormDefault="unqualified")
SOAPElement param = operation.addChildElement("organizationId");
param.addTextNode(String.valueOf(organizationId));
```

**Обработка SOAP Fault:**

```java
if (responseBody.hasFault()) {
    SOAPFault fault = responseBody.getFault();
    Detail detail = fault.getDetail();
    // Извлечение кода ошибки из CallerServiceFault
    int errorCode = extractErrorCode(detail); // 404, 400, 500
    throw new SoapClientException(errorCode, fault.getFaultString());
}
```

---

### 4. SOAP Caller (WildFly + JAX-WS)

**Роль:** SOAP Web Service с бизнес-операциями для управления организациями.

| Параметр | Значение |
|----------|----------|
| Технологии | WildFly 26.1.3, JAX-WS 2.3, JAXB, Apache HttpClient |
| Внешний порт | 8083 |
| Внутренний порт | 8080 |
| WSDL URL | `http://soap-caller:8080/soap/CallerServiceService?wsdl` |

**Функции:**
- Публикация SOAP Web Service через JAX-WS
- Бизнес-логика операций `fireAllOrgEmployees` и `acquireOrganization`
- HTTP-клиент для взаимодействия с Mule ESB
- Сериализация/десериализация XML через JAXB

**WSDL структура:**

```xml
<wsdl:service name="CallerServiceService">
  <wsdl:port name="CallerServicePort" binding="tns:CallerServiceBinding">
    <soap:address location="http://soap-caller:8080/soap/CallerServiceService"/>
  </wsdl:port>
</wsdl:service>

<wsdl:portType name="CallerService">
  <wsdl:operation name="fireAllOrgEmployees">
    <wsdl:input message="tns:fireAllOrgEmployees"/>
    <wsdl:output message="tns:fireAllOrgEmployeesResponse"/>
    <wsdl:fault name="CallerServiceException" message="tns:CallerServiceException"/>
  </wsdl:operation>
  <wsdl:operation name="acquireOrganization">...</wsdl:operation>
</wsdl:portType>
```

**Взаимодействие с Mule ESB:**

```java
public class MuleServiceClient {
    private final String baseUrl = "http://mule-esb:8082/api/v1";
    
    public Organization getOrganization(Long id) {
        HttpGet request = new HttpGet(baseUrl + "/organizations/" + id);
        request.setHeader("Accept", "application/xml");
        
        CloseableHttpResponse response = httpClient.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();
        
        if (statusCode >= 400) {
            // Пробрасываем HTTP статус в CallerServiceFault
            throw new CallerServiceException("Failed to get organization",
                new CallerServiceFault(statusCode, body));
        }
        
        return jaxbUnmarshal(body, Organization.class);
    }
}
```

---

### 5. Mule ESB 4.10.2 (Enterprise Service Bus)

**Роль:** Интеграционная шина предприятия для маршрутизации, трансформации и медиации сообщений.

| Параметр | Значение |
|----------|----------|
| Технологии | Mule Runtime 4.10.2 Enterprise, DataWeave 2.0 |
| Внешний порт | 8082 |
| Внутренний порт | 8082 |

**Функции:**
- Проксирование HTTP-запросов к Called-Web
- Protocol Mediation (HTTP ↔ HTTPS)
- Graceful Error Handling с propagation статус-кодов
- Health Check endpoint

**Архитектура Flows:**

```
┌─────────────────────────────────────────────────────────────────┐
│                      Mule ESB Flows                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              HTTP Listener (port 8082)                  │   │
│  │         Accepts: GET, POST, PUT, DELETE                 │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│         ┌────────────────────┼────────────────────┐            │
│         ▼                    ▼                    ▼            │
│  ┌─────────────┐     ┌─────────────┐      ┌─────────────┐     │
│  │ get-org     │     │ create-org  │      │ delete-org  │     │
│  │ flow        │     │ flow        │      │ flow        │     │
│  └──────┬──────┘     └──────┬──────┘      └──────┬──────┘     │
│         │                   │                    │             │
│         └───────────────────┴────────────────────┘             │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │            HTTP Requester (called-web:8443)             │   │
│  │                 HTTPS with SSL bypass                   │   │
│  └─────────────────────────────────────────────────────────┘   │
│                              │                                  │
│                              ▼                                  │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                    Error Handler                        │   │
│  │  ┌─────────────────┐ ┌─────────────────┐ ┌───────────┐ │   │
│  │  │ HTTP:NOT_FOUND  │ │ HTTP:BAD_REQUEST│ │    ANY    │ │   │
│  │  │   → 404         │ │    → 400        │ │   → 500   │ │   │
│  │  └─────────────────┘ └─────────────────┘ └───────────┘ │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

**Конфигурация HTTP Listener:**

```xml
<http:listener-config name="HTTP_Listener_config">
    <http:listener-connection host="0.0.0.0" port="8082"/>
</http:listener-config>
```

**Конфигурация HTTP Requester (к Called-Web):**

```xml
<http:request-config name="Called_Web_config">
    <http:request-connection 
        protocol="HTTPS" 
        host="called-web" 
        port="8443">
        <tls:context>
            <tls:trust-store insecure="true"/>
        </tls:context>
    </http:request-connection>
</http:request-config>
```

**Пример Flow с Error Handling:**

```xml
<flow name="get-organization-flow">
    <http:listener config-ref="HTTP_Listener_config" 
                   path="/api/v1/organizations/{id}" 
                   allowedMethods="GET">
        <http:response statusCode="#[vars.httpStatus default 200]">
            <http:headers><![CDATA[#[{'Content-Type': 'application/xml'}]]]></http:headers>
        </http:response>
        <http:error-response statusCode="#[vars.httpStatus default 500]">
            <http:headers><![CDATA[#[{'Content-Type': 'application/xml'}]]]></http:headers>
        </http:error-response>
    </http:listener>
    
    <http:request config-ref="Called_Web_config" 
                  method="GET" 
                  path="/soa/api/v1/organizations/{id}">
        <http:uri-params><![CDATA[#[{'id': attributes.uriParams.id}]]]></http:uri-params>
    </http:request>
    
    <error-handler>
        <on-error-continue type="HTTP:NOT_FOUND">
            <set-variable variableName="httpStatus" value="404"/>
            <set-payload value="#[error.errorMessage.payload]"/>
        </on-error-continue>
        <on-error-continue type="HTTP:BAD_REQUEST">
            <set-variable variableName="httpStatus" value="400"/>
            <set-payload value="#[error.errorMessage.payload]"/>
        </on-error-continue>
        <on-error-propagate type="ANY">
            <set-variable variableName="httpStatus" value="500"/>
            <ee:transform>
                <ee:set-payload><![CDATA[%dw 2.0
output application/xml writeDeclaration=false
---
{ appError: { code: 500, message: error.description } }]]></ee:set-payload>
            </ee:transform>
        </on-error-propagate>
    </error-handler>
</flow>
```

**Список Flows:**

| Flow Name | HTTP Method | Path | Target |
|-----------|-------------|------|--------|
| `health-check-flow` | GET | `/health` | Internal health |
| `get-organizations-flow` | GET | `/api/v1/organizations` | Get all orgs |
| `get-organization-flow` | GET | `/api/v1/organizations/{id}` | Get by ID |
| `create-organization-flow` | POST | `/api/v1/organizations` | Create org |
| `update-organization-flow` | PUT | `/api/v1/organizations/{id}` | Update org |
| `delete-organization-flow` | DELETE | `/api/v1/organizations/{id}` | Delete org |
| `compensate-organization-flow` | POST | `/api/v1/organizations/compensate` | Restore deleted |
| `get-employees-flow` | GET | `/api/v1/organizations/{orgId}/employees` | Get employees |
| `batch-update-employees-flow` | POST | `/api/v1/employees/batch/update` | Batch update |
| `batch-delete-employees-flow` | POST | `/api/v1/employees/batch/delete` | Batch delete |

---

### 6. Called-Web (WildFly 35)

**Роль:** REST API сервер с прокси к EJB-компонентам.

| Параметр | Значение |
|----------|----------|
| Технологии | WildFly 35, JAX-RS (RESTEasy), EJB Client |
| Внешний порт | 8444 (HTTPS) |
| Внутренний порт | 8443 (HTTPS) |

**Функции:**
- REST API для организаций и сотрудников
- EJB Remote Client для вызова бизнес-логики
- Service Discovery через Consul

**REST Endpoints:**

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/soa/api/v1/organizations` | Список организаций (пагинация, фильтры) |
| GET | `/soa/api/v1/organizations/{id}` | Получить организацию |
| POST | `/soa/api/v1/organizations` | Создать организацию |
| PUT | `/soa/api/v1/organizations/{id}` | Обновить организацию |
| DELETE | `/soa/api/v1/organizations/{id}` | Удалить организацию |
| POST | `/soa/api/v1/organizations/compensate` | Восстановить удалённую |
| GET | `/soa/api/v1/organizations/{orgId}/employees` | Сотрудники организации |
| POST | `/soa/api/v1/employees/batch/update` | Массовое обновление |
| POST | `/soa/api/v1/employees/batch/delete` | Массовое удаление |

**EJB Lookup через Consul:**

```java
@Startup
@Singleton
public class EjbClientProducer {
    
    @PostConstruct
    public void init() {
        // Получение адреса called-ejb через Consul
        String ejbHost = consulClient.getServiceAddress("called-ejb");
        
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, 
            "org.wildfly.naming.client.WildFlyInitialContextFactory");
        props.put(Context.PROVIDER_URL, 
            "remote+https://" + ejbHost + ":8443");
    }
}
```

---

### 7. Called-EJB (WildFly 35)

**Роль:** Бизнес-логика приложения в виде EJB-компонентов.

| Параметр | Значение |
|----------|----------|
| Технологии | WildFly 35, EJB 3.2, JPA 2.2, Hibernate |
| Внешний порт | 8446 (HTTPS) |
| Внутренний порт | 8443 (HTTPS) |

**Функции:**
- Stateless Session Beans для бизнес-операций
- JPA Entity Manager для работы с базой данных
- Transaction Management

**EJB Компоненты:**

```java
@Stateless
@Remote(OrganizationService.class)
public class OrganizationServiceImpl implements OrganizationService {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Organization findById(Long id) {
        return em.find(Organization.class, id);
    }
    
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Organization create(OrganizationRequest request) {
        Organization org = new Organization();
        // mapping...
        em.persist(org);
        return org;
    }
}
```

---

### 8. Service Discovery

#### Eureka Server (Spring Cloud Netflix)

**Роль:** Реестр сервисов для Spring Cloud компонентов.

| Параметр | Значение |
|----------|----------|
| Порт | 8761 |
| Dashboard | http://localhost:8761 |

**Зарегистрированные сервисы:**
- `rest-adapter`
- `zuul-gateway`
- `config-server`

#### Consul

**Роль:** Service Discovery для WildFly компонентов.

| Параметр | Значение |
|----------|----------|
| Порт | 8500 (HTTP), 8600 (DNS) |
| Dashboard | http://localhost:8500 |

**Зарегистрированные сервисы:**
- `called-ejb`
- `called-web`

---

### 9. Config Server (Spring Cloud Config)

**Роль:** Централизованное управление конфигурацией.

| Параметр | Значение |
|----------|----------|
| Порт | 8888 |
| Config URL | http://config-server:8888/{application}/{profile} |

**Конфигурации:**
- `rest-adapter.yml` - настройки REST Adapter

```yaml
spring:
  application:
    name: rest-adapter

soap-caller:
  url: http://soap-caller:8080/soap/CallerServiceService
```

---

## Сетевое взаимодействие

### Цепочка запроса `/orgmanager/api/v1/fire/all/{id}`

```
┌──────────┐     HTTPS      ┌──────────┐      HTTP       ┌──────────┐
│ Browser  │ ───────────────▶│  Nginx   │ ───────────────▶│   Zuul   │
│          │    :8448        │          │     :8080       │ Gateway  │
└──────────┘                 └──────────┘                 └────┬─────┘
                                                               │
                    ┌──────────────────────────────────────────┘
                    │ Eureka Lookup: rest-adapter
                    ▼
             ┌──────────────┐      SOAP/HTTP      ┌──────────────┐
             │ REST Adapter │ ───────────────────▶│ SOAP Caller  │
             │  :8080       │                     │   :8080      │
             └──────────────┘                     └──────┬───────┘
                                                         │
                    ┌────────────────────────────────────┘
                    │ HTTP (XML)
                    ▼
             ┌──────────────┐       HTTPS        ┌──────────────┐
             │   Mule ESB   │ ──────────────────▶│ Called-Web   │
             │    :8082     │                    │   :8443      │
             └──────────────┘                    └──────┬───────┘
                                                        │
                    ┌───────────────────────────────────┘
                    │ EJB/RMI over HTTPS
                    ▼
             ┌──────────────┐        JDBC        ┌──────────────┐
             │ Called-EJB   │ ──────────────────▶│  PostgreSQL  │
             │   :8443      │                    │    :5432     │
             └──────────────┘                    └──────────────┘
```

### Цепочка запроса `/soa/api/v1/organizations/{id}`

```
┌──────────┐     HTTPS      ┌──────────┐      HTTPS      ┌──────────┐
│ Browser  │ ───────────────▶│  Nginx   │ ───────────────▶│Called-Web│
│          │    :8448        │          │     :8443       │          │
└──────────┘                 └──────────┘                 └────┬─────┘
                                                               │
                                               EJB/RMI (HTTPS) │
                                                               ▼
                                                        ┌──────────────┐
                                                        │ Called-EJB   │
                                                        │   :8443      │
                                                        └──────┬───────┘
                                                               │ JDBC
                                                               ▼
                                                        ┌──────────────┐
                                                        │  PostgreSQL  │
                                                        │    :5432     │
                                                        └──────────────┘
```

---

## Обработка ошибок

### Propagation HTTP статус-кодов

Система обеспечивает сквозную передачу HTTP статус-кодов через все уровни:

```
Called-Web (404) → Mule ESB (404) → SOAP Caller (SOAP Fault code=404) 
                                  → REST Adapter (extracts 404) → Zuul (404) → Client
```

**Пример ответа при 404:**

```xml
<AppError>
    <code>404</code>
    <message>Organization not found: 999</message>
</AppError>
```

### Error Handling в каждом компоненте

| Компонент | Механизм | Действие |
|-----------|----------|----------|
| Called-Web | JAX-RS ExceptionMapper | Формирует `<appError>` XML |
| Mule ESB | error-handler + on-error-continue | Сохраняет payload, устанавливает statusCode |
| SOAP Caller | CallerServiceException + CallerServiceFault | Код в fault detail |
| REST Adapter | Парсинг SOAP Fault detail | Извлечение кода из `<code>` элемента |
| Zuul | Passthrough | Прозрачная передача |

---

## Запуск системы

```bash
# Полный запуск всех сервисов
docker compose up -d

# Проверка статуса
docker compose ps

# Просмотр логов
docker compose logs -f mule-esb
docker compose logs -f soap-caller
docker compose logs -f rest-adapter
```

### Health Check Endpoints

| Сервис | URL |
|--------|-----|
| Mule ESB | http://localhost:8082/health |
| REST Adapter | http://localhost:8445/orgmanager/actuator/health |
| SOAP Caller | http://localhost:8083 (WSDL) |
| Called-Web | https://localhost:8444/soa/api/v1/organizations |
| Eureka | http://localhost:8761 |
| Consul | http://localhost:8500 |
| Config Server | http://localhost:8888/actuator/health |

---

## Порты и протоколы

| Сервис | Внешний порт | Внутренний порт | Протокол |
|--------|--------------|-----------------|----------|
| Frontend | 8448 | 443 | HTTPS |
| Zuul Gateway | 8090 | 8080 | HTTP |
| REST Adapter | 8445 | 8080 | HTTP |
| SOAP Caller | 8083 | 8080 | HTTP (SOAP) |
| Mule ESB | 8082 | 8082 | HTTP |
| Called-Web | 8444 | 8443 | HTTPS |
| Called-EJB | 8446 | 8443 | HTTPS |
| PostgreSQL | 5432 | 5432 | TCP |
| Eureka | 8761 | 8761 | HTTP |
| Config Server | 8888 | 8888 | HTTP |
| Consul | 8500 | 8500 | HTTP |
| Swagger UI | 8080 | 8080 | HTTP |

---

## Технологический стек

| Категория | Технологии |
|-----------|------------|
| Frontend | React 19, TypeScript, Vite, Nginx |
| API Gateway | Netflix Zuul 1.3, Ribbon, Hystrix |
| REST Services | Spring Boot 2.3.12, JAX-RS (RESTEasy) |
| SOAP Services | JAX-WS 2.3, JAXB, SAAJ |
| Integration | Mule ESB 4.10.2, DataWeave 2.0 |
| Application Server | WildFly 35 (Backend), WildFly 26.1.3 (SOAP) |
| Service Discovery | Netflix Eureka, HashiCorp Consul |
| Configuration | Spring Cloud Config |
| Database | PostgreSQL 15, JPA/Hibernate |
| Containerization | Docker, Docker Compose |
