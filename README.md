# Order Management System

A Spring Boot application for managing orders with file-based processing, JWT authentication, and ActiveMQ message queue integration using Apache Camel.

## Features

- RESTful API for order management
- JWT-based authentication with role-based access control (USER, ADMIN)
- File-based order processing using Apache Camel
- ActiveMQ message queue integration
- Swagger/OpenAPI documentation
- Comprehensive logging with SLF4J
- Global exception handling

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- ActiveMQ broker (running on `localhost:61616`)

## Setup and Run Instructions

### 1. Clone and Build

```bash
cd c:\Projects\order
mvn clean install
```

### 2. Configure ActiveMQ

Ensure ActiveMQ is running and accessible at `tcp://localhost:61616`. Update the configuration in `src/main/resources/application.yaml` if needed:

```yaml
spring:
  activemq:
    broker-url: tcp://localhost:61616
    user: admin
    password: admin
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

Or using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Directory Structure

The application creates the following directories (if they don't exist):
- `input/orders/` - Place JSON order files here for processing
- `error/orders/` - Failed files are moved here

## API Usage Examples

### Authentication

#### Login

**Demo Credentials:**
- **Admin User:**
  - Username: `admin`
  - Password: `password` (results in ADMIN role)
- **Regular User:**
  - Any username/password combination (results in USER role)

```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Note:** 
- Use the returned token in the `Authorization` header for all subsequent API calls
- Admin user receives ADMIN role; all other users receive USER role
- In production, credentials should be stored securely in a database

#### Using the Token

Include the JWT token in the Authorization header:

```bash
Authorization: Bearer <your-token-here>
```

### Order APIs

All order APIs require authentication with USER or ADMIN role.

#### Create Order

```bash
POST http://localhost:8080/api/orders
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "customerId": "CUST001",
  "product": "Laptop",
  "amount": 15000.0
}
```

**Response (201 Created):**
```json
{
  "orderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "status": "CREATED"
}
```

#### Get Order by ID

```bash
GET http://localhost:8080/api/orders/{orderId}
Authorization: Bearer <your-token>
```

**Response (200 OK):**
```json
{
  "orderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customerId": "CUST001",
  "product": "Laptop",
  "amount": 15000.0,
  "status": "CREATED"
}
```

#### Get Orders by Customer ID

```bash
GET http://localhost:8080/api/orders?customerId=CUST001
Authorization: Bearer <your-token>
```

**Response (200 OK):**
```json
[
  {
    "orderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "customerId": "CUST001",
    "product": "Laptop",
    "amount": 15000.0,
    "status": "CREATED"
  }
]
```

### Swagger Documentation

Once the application is running, access the Swagger UI at:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

Use the "Authorize" button in Swagger UI to authenticate with your JWT token.

## Apache Camel Routes

The application uses Apache Camel for file-based order processing and message queue integration.

### 1. FileToQueueRoute (`file-to-queue`)

**Purpose:** Processes JSON order files from the file system and sends them to ActiveMQ queue.

**Configuration:**
- **Source:** `file:input/orders?noop=false`
- **Route ID:** `file-to-queue`
- **Target Queue:** `ORDER.CREATED.QUEUE`

**Flow:**
1. Polls files from `input/orders/` directory
2. Reads JSON file content
3. Unmarshals JSON to `Order` object using custom Jackson data format
4. Validates order:
   - `orderId` must not be null
   - `customerId` must not be null
   - `amount` must be greater than 0
5. Marshals Order back to JSON
6. Sends JSON message to ActiveMQ queue `ORDER.CREATED.QUEUE` as TextMessage
7. Logs successful processing

**Error Handling:**
- Invalid files are moved to `error/orders/` directory
- Errors are logged using SLF4J

**Example Order JSON File:**
```json
{
  "orderId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "customerId": "CUST001",
  "product": "Laptop",
  "amount": 15000.0,
  "status": "CREATED"
}
```

### 2. OrderQueueConsumer (`queue-consumer`)

**Purpose:** Consumes messages from ActiveMQ queue, processes orders, and logs order details.

**Configuration:**
- **Source:** `activemq:queue:ORDER.CREATED.QUEUE`
- **Route ID:** `queue-consumer`

**Flow:**
1. Consumes messages from `ORDER.CREATED.QUEUE`
2. Unmarshals JSON message to `Order` object using custom Jackson data format (`orderJacksonDataFormat`)
3. Processes order and logs details:
   - Order ID
   - Customer ID
   - Amount

**Log Format:**
```
Order processed | OrderId={orderId} | CustomerId={customerId} | Amount={amount}
```

**Message Acknowledgment:**
- Apache Camel automatically acknowledges messages after successful processing
- If processing fails (exception thrown), the message is not acknowledged and will be redelivered by the broker (based on broker redelivery policy)
- The acknowledgment happens automatically when the route completes successfully

## Project Structure

```
src/main/java/com/order/
├── apachecamel/          # Camel route definitions
│   ├── FileToQueueRoute.java
│   └── OrderQueueConsumer.java
├── config/               # Configuration classes
│   ├── ActiveMQConfig.java
│   ├── CamelJacksonConfig.java
│   ├── ObjectMapperConfig.java
│   ├── OpenApiConfig.java
│   └── SecurityConfig.java
├── controller/           # REST controllers
│   ├── AuthController.java
│   └── OrderController.java
├── dto/                  # Data Transfer Objects
│   ├── CreateOrderRequest.java
│   ├── CreateOrderResponse.java
│   ├── LoginRequest.java
│   └── LoginResponse.java
├── enums/                # Enumerations
│   └── OrderStatus.java
├── exception/            # Custom exceptions and handlers
│   ├── GlobalExceptionHandler.java
│   └── OrderNotFoundException.java
├── model/                # Domain models
│   └── Order.java
├── repository/           # Data access layer
│   └── OrderRepository.java
└── service/              # Business logic
    ├── JwtAuthenticationFilter.java
    ├── JwtService.java
    └── OrderService.java
```

## Configuration

### application.yaml

```yaml
spring:
  application:
    name: order
  activemq:
    broker-url: tcp://localhost:61616
    user: admin
    password: admin

jwt:
  secret: mySecretKey1234567890123456789012345678901234567890
  expiration: 86400000  # 24 hours in milliseconds

order:
  file:
    input:
      path: input/orders
    error:
      path: error/orders
    poll:
      interval: 5000
```

## Technologies Used

- **Spring Boot 4.0.1** - Application framework
- **Spring Security** - Authentication and authorization
- **JWT (JJWT)** - Token-based authentication
- **Apache Camel 4.7.0** - Integration framework
- **ActiveMQ** - Message broker
- **Jackson** - JSON processing
- **SLF4J** - Logging
- **Swagger/OpenAPI** - API documentation
- **Lombok** - Boilerplate code reduction

## Error Handling

The application uses `@RestControllerAdvice` for global exception handling:

- **OrderNotFoundException** → 404 Not Found
- **BadCredentialsException** → 401 Unauthorized
- **IllegalArgumentException** → 400 Bad Request
- **Generic Exception** → 500 Internal Server Error

All errors return a consistent JSON response format:
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 404,
  "error": "Order Not Found",
  "message": "Order not found with id: xxx"
}
```

## Logging

The application uses SLF4J for logging with appropriate log levels:

- **DEBUG** - Detailed diagnostic information
- **INFO** - Important business events and API calls
- **WARN** - Warning conditions
- **ERROR** - Error conditions with stack traces

## License

This project is for demonstration purposes.

