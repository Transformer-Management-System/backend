# Transformer Management System - Backend

Spring Boot backend for transformer thermal inspection workflows, including transformer management, inspection lifecycle, AI-assisted anomaly detection, and secure image handling with AWS S3 pre-signed URLs.

## Table of Contents
- [Overview](#overview)
- [Key Features](#key-features)
- [Tech Stack](#tech-stack)
- [Architecture at a Glance](#architecture-at-a-glance)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Database Notes](#database-notes)
- [Run the Application](#run-the-application)
- [API Documentation](#api-documentation)
- [API Surface Summary](#api-surface-summary)
- [Authentication & Authorization](#authentication--authorization)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)

## Overview
This service exposes REST APIs under `/api/v1/**` for:
- transformer records,
- inspections linked to transformers,
- anomaly annotations,
- AI-based analysis of inspection images,
- signed upload/download URL generation for private S3 objects.

It acts as:
- an OAuth2 resource server (JWT validation against Keycloak),
- a PostgreSQL-backed persistence layer via Spring Data JPA,
- an integration layer to a FastAPI anomaly-detection microservice,
- an S3 pre-signed URL provider for direct client upload/download flows.

## Key Features
- **Transformer lifecycle APIs** (CRUD).
- **Inspection lifecycle APIs** (CRUD per transformer).
- **Annotation APIs** for detected/manual anomalies.
- **Anomaly analysis endpoint** that calls FastAPI and persists detection metadata.
- **Pre-signed S3 URLs** for secure client-side image upload/download.
- **OpenAPI/Swagger UI** for interactive API exploration.
- **Legacy profile endpoints** for annotation logs and maintenance records export flows.
- **Unified API response envelope** via `ApiResponse<T>`.

## Tech Stack
- **Java 21**
- **Spring Boot 3.4.1**
- Spring Web, Spring Data JPA, Validation, Security, OAuth2 Resource Server, Actuator
- Spring WebFlux `WebClient` (FastAPI integration)
- PostgreSQL (runtime)
- H2 (tests)
- AWS SDK v2 (S3 pre-signing)
- springdoc-openapi (Swagger UI)
- Maven Wrapper (`./mvnw`)

## Architecture at a Glance
1. Client uploads image to S3 using a backend-generated pre-signed PUT URL.
2. Client stores S3 object key through transformer/inspection APIs.
3. `POST /api/v1/inspections/{id}/analyze`:
   - backend generates pre-signed GET URLs for baseline and maintenance images,
   - backend generates pre-signed PUT URL for AI-annotated image,
   - backend calls FastAPI `/api/v1/detect`,
   - backend stores image-level and anomaly-level detection results in PostgreSQL.

## Project Structure
```text
/home/runner/work/backend/backend
├── pom.xml
├── docs/
│   └── api-examples.md
├── src/main/java/com/chamikara/spring_backend
│   ├── config/          # Security, S3, OpenAPI, WebClient configs
│   ├── controller/      # REST controllers
│   ├── dto/             # Request/response DTOs
│   ├── entity/          # JPA entities
│   ├── exception/       # Global exception handling
│   ├── repository/      # Spring Data repositories
│   ├── security/        # JWT -> local user sync/auth principal
│   └── service/         # Business logic + integrations
├── src/main/resources
│   ├── application.properties
│   └── db/migration/    # SQL migration scripts (manual usage notes)
└── src/test
```

## Prerequisites
- **JDK 21** (required by `pom.xml` release target)
- Maven (optional if using `./mvnw`)
- PostgreSQL instance
- Keycloak realm / JWT issuer
- AWS S3 bucket and credentials
- FastAPI detection service

## Configuration
Primary configuration is in:
`/home/runner/work/backend/backend/src/main/resources/application.properties`

You can keep secrets out of source control by using environment variables (`.env` is gitignored).

### Important properties

| Property | Purpose | Default / Example |
|---|---|---|
| `server.port` | HTTP port | `8080` |
| `spring.datasource.url` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/transformer_db` |
| `spring.datasource.username` | DB username | `postgres` |
| `spring.datasource.password` | DB password | `1234` (change for local/prod) |
| `spring.jpa.hibernate.ddl-auto` | Schema strategy | `validate` |
| `fastapi.service.url` | FastAPI base URL | `http://localhost:8000` |
| `fastapi.service.detect-endpoint` | Detect path config | `/api/v1/detect` |
| `fastapi.service.timeout` | Detect call timeout (ms) | `300000` |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | JWT issuer URI | `http://localhost:9090/realms/transformer-realm` |
| `aws.accessKeyId` | AWS access key | `${AWS_ACCESS_KEY_ID:CHANGE_ME}` |
| `aws.secretKey` | AWS secret key | `${AWS_SECRET_ACCESS_KEY:CHANGE_ME}` |
| `aws.region` | AWS region | `${AWS_REGION:us-east-1}` |
| `aws.s3.bucketName` | S3 bucket | `${AWS_S3_BUCKET_NAME:CHANGE_ME}` |

## Database Notes
- JPA is configured with `ddl-auto=validate`, so expected tables must already exist.
- The repository includes SQL scripts in:
  - `/home/runner/work/backend/backend/src/main/resources/db/migration/V2__add_detection_columns.sql`
  - `/home/runner/work/backend/backend/src/main/resources/db/migration/V3__add_annotated_image_key.sql`
- These scripts are written as manual migration steps and should be applied before startup when relevant.

## Run the Application
From `/home/runner/work/backend/backend`:

```bash
chmod +x ./mvnw
./mvnw spring-boot:run
```

Run with legacy profile enabled:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=legacy
```

Build JAR:

```bash
./mvnw clean package
```

## API Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Detailed request/response examples:  
  `/home/runner/work/backend/backend/docs/api-examples.md`

## API Surface Summary
All `/api/v1/**` routes require JWT unless stated otherwise.

### Core endpoints
- `GET/POST/PUT/DELETE /api/v1/transformers`
- `GET/POST/PUT/DELETE /api/v1/inspections` (path variants under transformer)
- `GET/POST /api/v1/inspections/{inspectionId}/anomalies`
- `PUT/DELETE /api/v1/anomalies/{id}`
- `POST /api/v1/inspections/{id}/analyze`
- `GET /api/v1/images/generate-upload-url`
- `GET /api/v1/images/generate-download-url`

### Legacy profile endpoints (`legacy` profile only)
- `/api/v1/annotation-logs/**`
- `/api/v1/records/**`

### Public endpoints
- `GET /actuator/health`
- `GET /actuator/info`

## Authentication & Authorization
- App is stateless (`SessionCreationPolicy.STATELESS`).
- Security rules:
  - `/api/v1/**` -> authenticated
  - `/actuator/health`, `/actuator/info` -> public
- JWT is validated using configured issuer URI.
- JWT claims are used to upsert a local `users` table record (`LocalUserSyncService`).
- CORS currently allows `http://localhost:5173`.

## Testing
From `/home/runner/work/backend/backend`:

```bash
./mvnw test
```

Test profile uses:
- H2 in-memory database (`src/test/resources/application-test.properties`)
- separate test app name and security test configuration

## Troubleshooting
- **`release version 21 not supported`**: your runtime JDK is lower than 21; install/use Java 21.
- **401 on `/api/v1/**`**: verify `Authorization: Bearer <token>` and JWT issuer configuration.
- **S3 URL generation issues**: verify AWS credentials, region, and bucket env vars.
- **Analyze endpoint failures**: verify FastAPI service URL, timeout, and reachability.

