# Reservation Management System - Backend

This is a Spring Boot 3 backend application for a reservation management system using Java 17, PostgreSQL, and Maven.

## Features

- **User Management**: ADMIN, TEACHER, STUDENT roles
- **Room Management**: Room booking and capacity management
- **Event Management**: COURSE, DEFENSE, MEETING events
- **Availability Management**: Teacher availability tracking
- **Conflict Detection**: Automatic conflict detection between events
- **JWT Authentication**: Secure authentication with JWT tokens
- **CORS Configuration**: Configured for frontend at localhost:3000

## Prerequisites

- Java 17
- Maven 3.6+
- PostgreSQL 12+

## Setup Instructions

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE reservation_db;
CREATE USER postgres WITH PASSWORD 'MY_PASSWORD';
GRANT ALL PRIVILEGES ON DATABASE reservation_db TO postgres;
```

### 2. Configuration

Update `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/reservation_db
spring.datasource.username=postgres
spring.datasource.password=MY_PASSWORD
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false
server.port=8080

app.security.jwt.secret=CHANGE_ME_SECRET_KEY_256BIT_MIN
app.security.jwt.expiration=3600000
```

**Important**: Change the JWT secret to a secure 256-bit key in production.

### 3. Run the Application

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login

### Users
- `GET /api/users` - Get all users
- `POST /api/users` - Create a new user
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/role/{role}` - Get users by role

### Rooms
- `GET /api/rooms` - Get all rooms
- `POST /api/rooms` - Create a new room
- `GET /api/rooms/{id}` - Get room by ID
- `GET /api/rooms/capacity/{minCapacity}` - Get rooms by minimum capacity

### Events
- `GET /api/events` - Get all events
- `POST /api/events` - Create a new event
- `GET /api/events/{id}` - Get event by ID
- `POST /api/events/generate` - Auto-generate schedules from unscheduled events
- `PUT /api/events/{id}/reschedule` - Reschedule an event (change date/time)
- `PUT /api/events/{id}/change-room` - Change event room

### Availabilities
- `GET /api/availabilities` - Get all teacher availabilities
- `POST /api/availabilities` - Create teacher availability
- `GET /api/availabilities/{id}` - Get availability by ID
- `GET /api/availabilities/teacher/{teacherId}` - Get availabilities for a specific teacher
- `GET /api/availabilities/date/{date}` - Get all availabilities for a specific date

### Conflicts
- `GET /api/conflicts` - Get all detected conflicts
- `POST /api/conflicts/detect` - Run conflict detection and save results

## Core Features

### 1. Automatic Schedule Generation

The system can automatically generate schedules for unscheduled events based on:

- **Teacher Availability**: Uses the Availability entity to find possible time slots
- **Room Availability**: Checks room capacity and availability (no overlapping events)
- **Teacher Conflicts**: Ensures no overlapping events for the same teacher
- **Room Assignment**: Assigns the first available room that meets capacity requirements

**Usage Example:**
```json
POST /api/events/generate
{
  "events": [
    {
      "type": "COURSE",
      "teacherId": 1,
      "title": "Database Systems",
      "expectedParticipants": 30,
      "preferredDates": ["2025-08-20", "2025-08-21"],
      "preferredStartTime": "09:00",
      "preferredEndTime": "11:00"
    }
  ]
}
```

### 2. Conflict Detection

The system automatically detects three types of conflicts:

- **ROOM Conflicts**: Same room booked for overlapping time periods
- **TEACHER Conflicts**: Same teacher assigned to overlapping events  
- **CAPACITY Conflicts**: Expected participants exceed room capacity

**Conflict Detection Logic:**
- Compares all scheduled events for overlapping times on the same date
- Identifies room double-bookings
- Detects teacher scheduling conflicts
- Validates room capacity against expected participants

### 3. Event Management

- **Create Events**: Manual event creation with validation
- **Reschedule Events**: Change date/time with automatic conflict checking
- **Change Rooms**: Move events to different rooms with availability validation
- **Schedule Generation**: Bulk scheduling of multiple unscheduled events

## Project Structure

```
src/main/java/com/reservation/
├── config/                 # Configuration classes
├── controller/             # REST controllers
├── dto/                    # Data Transfer Objects
│   ├── request/           # Request DTOs
│   └── response/          # Response DTOs
├── exception/             # Exception handling
├── model/                 # JPA entities and enums
│   ├── entity/           # JPA entities
│   └── enums/            # Enum types
├── repository/            # JPA repositories
├── service/               # Business logic services
└── util/                  # Utility classes
```

## Entity Relationships

- **User**: Base entity for all users (Admin, Teacher, Student)
- **Room**: Physical rooms available for booking
- **Event**: Scheduled events (Course, Defense, Meeting)
- **Availability**: Teacher availability slots
- **Conflict**: Detected conflicts between events

## Security

- JWT-based authentication
- Password hashing with BCrypt
- CORS enabled for localhost:3000
- Role-based access control

## Development

### Adding New Features

1. Create entities in `model/entity/`
2. Add repositories in `repository/`
3. Implement services in `service/`
4. Create DTOs in `dto/request/` and `dto/response/`
5. Add controllers in `controller/`

### Database Schema

The application uses Hibernate DDL auto-update mode, so the database schema will be created/updated automatically when you run the application.

## Testing

Run tests with:

```bash
mvn test
```

## Production Deployment

1. Set `spring.jpa.hibernate.ddl-auto=validate` in production
2. Use a strong JWT secret (256-bit minimum)
3. Configure proper CORS origins
4. Set up proper logging configuration
5. Use environment variables for sensitive configuration
