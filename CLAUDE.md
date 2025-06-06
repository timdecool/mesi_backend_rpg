# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot backend for an RPG module creation and sharing platform. The application provides:

- **Module Management**: Create, edit, and manage RPG modules with different block types (text, images, music, stats)
- **Collaborative Editing**: Real-time collaborative editing using WebSockets 
- **AI Integration**: Anthropic Claude integration for content generation
- **User Management**: Firebase Authentication with custom user profiles
- **File Storage**: Handle image and file uploads
- **Rating & Comments**: Module rating and commenting system

## Development Commands

### Local Development
```bash
# Build and run with Maven
mvn clean install
mvn spring-boot:run

# Run tests
mvn test

# Run single test class
mvn test -Dtest=MesiBackendRpgApplicationTests
```

### Docker Development
```bash
# Build and start all services (app + MySQL + phpMyAdmin)
docker compose build
docker compose up

# Rebuild after changes
docker compose down -v
docker compose build
docker compose up

# Access phpMyAdmin at http://localhost:2001
# Application runs on http://localhost:8080
# MySQL exposed on port 3307
```

## Architecture Overview

### Core Domain Models
- **Module**: Main RPG content container with versioning support
- **Block**: Content blocks (Paragraph, Picture, Music, Stat, IntegratedModule)
- **User**: User management with Firebase integration 
- **GameSystem**: RPG system classification (D&D, Pathfinder, etc.)

### Key Architectural Patterns
- **DTO Layer**: Complete separation between API contracts (DTOs) and domain models
- **Service Layer**: Business logic encapsulation with transaction management
- **Repository Layer**: JPA repositories for data access
- **Mapper Pattern**: MapStruct-style manual mapping between models and DTOs

### Authentication & Security
- Firebase Authentication via custom `FirebaseAuthenticationFilter`
- JWT token validation for all non-public endpoints
- CORS configured for `http://localhost:4200` and `https://jdr-cli.vercel.app`
- Public endpoints: `/api/public/**` and `/ws/**`

### WebSocket Integration
- Real-time collaborative editing through WebSocket connections
- Configured in `WebSocketConfig` and `WebSocketSecurityConfig`
- Endpoints under `/ws/**`

### AI Integration
- Anthropic Claude API integration via `AnthropicService`
- Retry logic with exponential backoff for rate limiting
- Content generation for modules and blocks

## Required Configuration Files

The application requires two critical configuration files in `src/main/resources/`:

1. **application.properties**: Database connection, Firebase config, API keys
2. **firebase-service-account.json**: Firebase service account credentials

These files contain sensitive information and are not committed to the repository.

## Database Setup

- **Development**: MySQL 8.0.19 via Docker Compose
- **Connection**: `jdbc:mysql://db:3306/mesi_rpg` (Docker) or `localhost:3307` (host)
- **Credentials**: root/root (development only)

## Key Dependencies

- Spring Boot 3.4.4 (Java 17)
- Spring Data JPA with MySQL
- Spring Security + Firebase Admin SDK
- Spring WebFlux (for external API calls)
- Spring WebSocket (collaborative editing)
- Lombok (code generation)
- JSch (SSH connectivity)

## Development Notes

### Module Block System
The application uses a polymorphic block system where different content types (`ParagraphBlock`, `PictureBlock`, `MusicBlock`, `StatBlock`) extend the base `Block` entity. This allows flexible content composition within modules.

### Version Management
Modules support versioning through the `ModuleVersion` entity, enabling content history and rollback capabilities.

### Collaborative Features
Real-time editing is implemented through WebSocket connections, allowing multiple users to edit modules simultaneously with cursor position tracking.

### AI Content Generation
The `AnthropicService` provides AI-powered content generation with proper error handling and rate limiting. The service includes curl command logging for debugging API calls.

## Testing

- Main test class: `MesiBackendRpgApplicationTests`
- Use `mvn test` to run the full test suite
- Individual tests can be run with `-Dtest=ClassName`

## Startup Behavior

The application includes a `CommandLineRunner` that:
1. Creates default test users and game systems
2. Initializes Firebase test user
3. Generates Firebase ID token for development/testing
4. Logs the token to console for use in API testing tools