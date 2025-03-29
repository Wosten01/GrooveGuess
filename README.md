# GrooveGuess / BitsBlitz - Music Quiz Web App

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.20-blue.svg)](https://kotlinlang.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-blue.svg)](https://www.postgresql.org)

## Project Overview

A web application for music quizzes where users:
- Select quizzes and guess playing tracks within time limits
- Earn points for correct answers
- Compete in global rankings

## Key Features

### User System
- JWT authentication (Register/Login)
- Role-based access (Admin/User)
- User profiles with scores

### Admin Features
- Track management (CRUD operations)
- Quiz creation/editing:
  - Set rounds count
  - Add tracks (roundCount × 4 tracks required)
  
### Quiz Mechanics
- Multiple rounds per quiz
- 4 tracks per round (1 plays, 1 correct)
- +10 points per correct answer

## Technical Stack

### Backend
- Kotlin 1.9 + Spring Boot 3.2
- Spring Security + JWT
- PostgreSQL + Hibernate
- Flyway migrations
- Dockerized deployment

### Frontend (Separate Repo)
- Vue.js + TypeScript
- Axios for API calls

## Infrastructure
- REST API architecture
- Swagger/OpenAPI documentation
- CI/CD ready (Docker Compose)

## Getting Started
```bash
# Backend setup
docker-compose up -d  # Starts PostgreSQL
./gradlew bootRun     # Starts Spring Boot