# GrooveGuess - Interactive Music Quiz Platform

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-blue.svg)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-blue.svg)](https://www.postgresql.org)
[![React](https://img.shields.io/badge/React-18-61DAFB.svg)](https://reactjs.org)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6.svg)](https://www.typescriptlang.org)
[![Redis](https://img.shields.io/badge/Redis-6.2-DC382D.svg)](https://redis.io)
[![Python](https://img.shields.io/badge/Python-3.9-3776AB.svg)](https://www.python.org)





## Project Overview

GrooveGuess is a full-stack web application that challenges users' music knowledge through interactive quizzes. Players listen to music tracks and guess the correct title from multiple options, earning points for correct answers and competing on a global leaderboard.

## Screenshots

### Welcome
<img width="1511" alt="Screenshot 2025-05-03 at 23 32 34" src="https://github.com/user-attachments/assets/f3c60928-67ba-4340-a547-0a6462ffcd4e" />

### Quizzes
<img width="1508" alt="Screenshot 2025-05-03 at 23 33 33" src="https://github.com/user-attachments/assets/548a9e1e-5691-450b-87b6-2bd3d8fd6240" />

#### Player
<img width="1502" alt="Screenshot 2025-05-03 at 23 33 59" src="https://github.com/user-attachments/assets/31999f1a-b64e-4029-b302-e3d86125a39f" />

### Stats
<img width="743" alt="Screenshot 2025-05-03 at 23 34 28" src="https://github.com/user-attachments/assets/15048d8b-09ce-4e37-827d-cad9b6cddc66" />

![image](https://github.com/user-attachments/assets/b7987930-b06b-4305-8cf2-d9f4d433be2c)



## Key Features

### User Experience
- Engaging quiz gameplay with timed rounds
- Real-time scoring and feedback
- Personalized user profiles with statistics
- Global leaderboard to track top performers

### User Management
- Secure JWT authentication system
- Role-based access control (Admin/User)
- User profiles with performance tracking

### Admin Capabilities
- Comprehensive track management (CRUD operations)
- Quiz creation and editing tools
- User management dashboard

### Quiz Mechanics
- Multiple rounds per quiz with configurable difficulty
- Each round presents 4 track options (1 plays, user selects the correct one)
- Time-limited rounds for added challenge
- +10 points awarded for each correct answer
- Detailed results and statistics after completion

## Technical Architecture

### Backend (Kotlin + Spring Boot)
- **Core Framework**: Kotlin 1.9 with Spring Boot 3.2
- **Security**: Spring Security with JWT token authentication
- **Database**: PostgreSQL with Hibernate ORM
- **Migration**: Flyway for database schema management
- **Caching & Session Management**: Redis for game state persistence and session handling
- **API Documentation**: Swagger/OpenAPI
- **Testing**: JUnit 5 with Mockito for unit and integration tests

### Frontend (React + TypeScript)
- **Core Framework**: React 18 with TypeScript 5
- **State Management**: React Context API
- **UI Components**: Material-UI (MUI) with custom theming
- **HTTP Client**: Axios for API communication
- **Animations**: Framer Motion for smooth transitions
- **Audio**: Custom audio player implementation
- **Responsive Design**: Mobile-first approach with adaptive layouts

### DevOps & Infrastructure
- **Containerization**: Docker with Docker Compose
- **API Architecture**: RESTful endpoints with proper status codes
- **Error Handling**: Comprehensive exception handling and logging
- **Deployment**: Ready for cloud deployment (AWS, GCP, Azure)
- **Static File Server**: Lightweight Python HTTP server for music file delivery
- **Redis**: Used for session management, game state persistence, and caching

## Redis Implementation

Redis serves multiple critical functions in the application:

1. **Game Session Management**: Stores active game sessions with expiration times
2. **Leaderboard Caching**: Efficiently manages and sorts global leaderboards
3. **Rate Limiting**: Prevents API abuse by tracking request frequencies
4. **Temporary Data Storage**: Stores game state between rounds
5. **Pub/Sub Messaging**: Enables real-time updates for multiplayer features

## Music File Server

A lightweight Python-based HTTP server handles music file delivery:

- **Simple Implementation**: Custom Python HTTP server using the built-in `http.server` module
- **CORS Support**: Configured with proper CORS headers to allow frontend access
- **Docker Integration**: Runs in its own container for isolation and scalability

## Getting Started

### Prerequisites
- JDK 17 or higher
- Node.js 16 or higher
- Docker and Docker Compose
- PostgreSQL (if running locally)
- Redis (if running locally)
- Python 3.9 or higher (for music file server)

### Backend Setup
```bash
# Clone the repository
git clone https://github.com/yourusername/grooveguess.git
cd grooveguess/backend

# Start dependencies with Docker
docker-compose up -d

# Build and run the application
./gradlew bootRun

# The API will be available at http://localhost:8080
# Swagger documentation: http://localhost:8080/swagger-ui.html
```

### Frontend Setup
```bash
# Navigate to frontend directory
cd ../frontend

# Install dependencies
npm install

# Start development server
npm start

# The application will be available at http://localhost:3000
```

### Music File Server Setup
```bash
# Navigate to music-server directory
cd ../music-server

# Start the Python server with Docker
docker build -t music-server .
docker run -d -p 8000:8000 -v /path/to/your/music:/app/music music-server

# Music files will be served from http://localhost:8000
```

### Production Deployment
```bash
# Deploy all services with Docker Compose
docker-compose -f docker-compose.prod.yml up -d
```

## Project Structure

### Backend
```
backend/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/grooveguess/backend/
│   │   │       ├── api/
│   │   │       ├── config/
│   │   │       │   ├── RedisConfig.kt
│   │   │       │   └── SecurityConfig.kt
│   │   │       ├── domain/
│   │   │       ├── exception/
│   │   │       ├── security/
│   │   │       └── service/
│   │   │           └── GameService.kt
│   │   └── resources/
│   │       ├── db/migration/
│   │       └── application.yml
│   └── test/
└── build.gradle.kts
```

### Frontend
```
frontend/
├── public/
├── src/
│   ├── api/
│   ├── components/
│   ├── context/
│   ├── hooks/
│   ├── pages/
│   ├── theme/
│   ├── translations/
│   └── App.tsx
└── package.json
```

### Music File Server
```
music-server/
├── server.py
├── Dockerfile
└── docker-compose.yml
```

## License
 License? don't think so.
