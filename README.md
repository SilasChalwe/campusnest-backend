# CampusNest Backend

A comprehensive Spring Boot application for student accommodation finding and management, providing secure REST APIs for students, landlords, and administrators.

## 🏠 Overview

CampusNest solves the problem of finding safe, affordable housing near campus by connecting students with verified landlords through a secure, real-time platform. The backend provides robust APIs for property management, booking systems, secure payments, and real-time messaging.

## ✨ Key Features

### For Students
- Search and filter accommodations by location, price, and amenities
- Book properties with real-time availability
- Secure payment processing and rent management
- Real-time chat with landlords
- Review and rate properties

### For Landlords
- Property and unit management
- Tenant application processing
- Payment tracking and reporting
- Tenant communication
- Availability calendar management

### For Administrators
- User verification and management
- System monitoring and analytics
- Dispute resolution
- Content moderation

## 🛠 Tech Stack

- **Java 17+**
- **Spring Boot 3.x** (Web, Data JPA, Security)
- **Spring Security** with JWT authentication
- **WebSocket/STOMP** for real-time features
- **PostgreSQL** (production) / H2 (development)
- **Flyway** for database migrations
- **Redis** for caching and session management
- **Docker & Docker Compose** for containerization
- **Swagger/OpenAPI 3** for API documentation

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Docker and Docker Compose
- Maven 3.6+

### 1. Clone the Repository
```bash
git clone <repository-url>
cd campusnest-backend
```

### 2. Environment Setup
Create a `.env` file in the root directory:
```env
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=campusnest_db
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_key_here
JWT_EXPIRATION=86400000

# Email Configuration
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your_email@gmail.com
SMTP_PASSWORD=your_app_password

# SMS Configuration (optional)
SMS_API_KEY=your_sms_api_key
SMS_API_SECRET=your_sms_api_secret

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# File Storage (AWS S3 or compatible)
AWS_ACCESS_KEY=your_access_key
AWS_SECRET_KEY=your_secret_key
S3_BUCKET_NAME=campusnest-files
S3_REGION=us-east-1
```

### 3. Run with Docker Compose
```bash
# Start all services (PostgreSQL, Redis, Application)
docker-compose up -d

# View logs
docker-compose logs -f campusnest-app
```

### 4. Run Locally (Development)
```bash
# Start only PostgreSQL and Redis
docker-compose up -d postgres redis

# Run the application
./mvnw spring-boot:run

# Or with Maven wrapper
mvn spring-boot:run
```

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication
The API uses JWT Bearer tokens for authentication:
```bash
Authorization: Bearer <your_jwt_token>
```

### Interactive API Documentation
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Key Endpoints

#### Authentication
```http
POST /api/v1/auth/register          # Register new user
POST /api/v1/auth/login             # User login
POST /api/v1/auth/refresh           # Refresh access token
POST /api/v1/auth/verify/email      # Verify email address
POST /api/v1/auth/verify/phone      # Verify phone number
POST /api/v1/auth/logout            # Logout user
```

#### Properties
```http
GET    /api/v1/properties           # Search properties
POST   /api/v1/properties           # Create property (Landlord)
GET    /api/v1/properties/{id}      # Get property details
PUT    /api/v1/properties/{id}      # Update property (Owner)
DELETE /api/v1/properties/{id}      # Delete property (Owner/Admin)
```

#### Bookings
```http
POST /api/v1/bookings               # Create booking request (Student)
GET  /api/v1/bookings               # List user bookings
GET  /api/v1/bookings/{id}          # Get booking details
PUT  /api/v1/bookings/{id}/approve  # Approve booking (Landlord)
PUT  /api/v1/bookings/{id}/reject   # Reject booking (Landlord)
```

#### Payments
```http
POST /api/v1/payments/checkout      # Create payment intent
GET  /api/v1/payments/{id}          # Get payment details
GET  /api/v1/payments               # List payments
```

#### Chat & Messaging
```http
GET  /api/v1/chats                  # List conversations
POST /api/v1/chats/start            # Start new conversation
GET  /api/v1/chats/{id}/messages    # Get conversation messages
POST /api/v1/chats/{id}/messages    # Send message
```

## 🏗 Project Structure

```
src/main/java/com/nextinnomind/campusnestbackend/
├── config/                 # Configuration classes
│   ├── SecurityConfig.java
│   ├── WebSocketConfig.java
│   ├── OpenApiConfig.java
│   └── ...
├── controller/             # REST controllers
│   ├── AuthController.java
│   ├── PropertyController.java
│   ├── BookingController.java
│   └── ...
├── dto/                    # Data Transfer Objects
│   ├── auth/
│   ├── property/
│   ├── booking/
│   └── ...
├── entity/                 # JPA entities
│   ├── User.java
│   ├── Property.java
│   ├── BookingRequest.java
│   └── ...
├── repository/             # Spring Data JPA repositories
├── service/                # Business logic services
├── security/               # Security-related classes
├── exception/              # Custom exceptions and handlers
├── util/                   # Utility classes
└── websocket/              # WebSocket configuration and handlers
```

## 🗄 Database Schema

The application uses Flyway migrations located in `src/main/resources/db/migration/`:

- `V1__Create_users_table.sql` - Users and authentication
- `V2__Create_properties_table.sql` - Property listings
- `V3__Create_units_table.sql` - Individual units/rooms
- `V4__Create_bookings_table.sql` - Booking requests
- `V5__Create_payments_table.sql` - Payment transactions
- `V6__Create_messages_table.sql` - Chat messages
- `V7__Create_reviews_table.sql` - Property reviews
- `V8__Create_tokens_table.sql` - Refresh tokens and verification tokens

## 🔐 Security

### Authentication & Authorization
- JWT-based authentication with access and refresh tokens
- Role-based access control (STUDENT, LANDLORD, ADMIN)
- Password hashing with BCrypt
- Email and phone verification required

### API Security
- HTTPS enforced in production
- CORS properly configured
- Rate limiting on authentication endpoints
- Input validation and sanitization
- SQL injection prevention through JPA

### Data Protection
- Sensitive data encryption at rest
- Secure token storage
- User data anonymization options
- GDPR compliance considerations

## 🔄 Real-time Features

### WebSocket Integration
Real-time functionality powered by Spring WebSocket with STOMP:

- **Chat Messaging**: Instant message delivery
- **Booking Updates**: Real-time status notifications
- **Payment Notifications**: Transaction status updates
- **Property Updates**: Availability changes

### Connection Flow
1. Client authenticates via JWT during WebSocket handshake
2. Server maps session to user ID
3. Client subscribes to relevant channels:
    - `/topic/user/{userId}` - Personal notifications
    - `/topic/chat/{conversationId}` - Chat messages

## 📊 Monitoring & Logging

### Health Checks
```http
GET /actuator/health        # Application health status
GET /actuator/metrics       # Application metrics
GET /actuator/info          # Application information
```

### Logging
- Structured logging with SLF4J and Logback
- Separate log levels for different environments
- Request/response logging for API calls
- Security event logging

## 🧪 Testing

### Run Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthControllerTest

# Run integration tests
./mvnw test -Dtest="**/*IntegrationTest"
```

### Test Coverage
```bash
# Generate coverage report
./mvnw jacoco:report

# View report at: target/site/jacoco/index.html
```

## 🚀 Deployment

### Docker Production Build
```bash
# Build production image
docker build -t campusnest-backend:latest .

# Run with production profile
docker run -e SPRING_PROFILES_ACTIVE=prod campusnest-backend:latest
```

### Environment Profiles
- `dev` - Development with H2 database
- `test` - Testing with in-memory database
- `prod` - Production with PostgreSQL

### Production Checklist
- [ ] Configure proper SSL certificates
- [ ] Set up database backups
- [ ] Configure monitoring and alerting
- [ ] Set up log aggregation
- [ ] Configure rate limiting
- [ ] Set up CDN for static assets
- [ ] Configure auto-scaling

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Java coding standards
- Write comprehensive tests
- Update documentation for new features
- Use meaningful commit messages
- Ensure all tests pass before submitting PR

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

- **Documentation**: Check the Swagger UI at `/swagger-ui.html`
- **Issues**: Report bugs and feature requests via GitHub Issues
- **Email**: mchalwesilas@gmail.com

## 🔄 Version History

### v1.0.0 (Current)
- Initial release with core functionality
- User authentication and authorization
- Property and booking management
- Real-time chat and notifications
- Payment integration
- Admin panel featuresfre 