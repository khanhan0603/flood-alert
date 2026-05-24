# Flood Alert & Rescue System (Backend)

## Description
This project is the backend for a **flood monitoring and emergency support system**.

It is being developed to:
- Collect water level data from sensors
- Detect potential flood risks
- Support emergency rescue requests (SOS)

> This project is currently under development.

---

## Tech Stack
- Java (Spring Boot)
- PostgreSQL
- Spring Data JPA
- Maven

---

## Features (Planned)
- Receive sensor data (water level)
- Store and manage flood data
- Detect abnormal conditions and trigger alerts
- SOS system for people in affected areas

---

## Project Status
### Done
- [x] Initial setup (Spring Boot, PostgreSQL)

### In Progress
- [ ] API development
- [ ] Database design

### Planned
- [ ] IoT integration
- [ ] Alert system

---

## Run Project

```bash
mvn spring-boot:run
```

---

## Deploy Backend to Render

This repo includes Render deployment files:

- `Dockerfile`: builds and runs the Spring Boot app with Java 21
- `render.yaml`: creates the API service, PostgreSQL database, and Redis-compatible Render Key Value instance
- `/health`: lightweight health check endpoint for Render

### Steps

1. Push this repository to GitHub.
2. In Render, choose **New > Blueprint**.
3. Connect the GitHub repository and select the branch containing `render.yaml`.
4. Apply the Blueprint.
5. After the PostgreSQL database is created, open the database shell and run:

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

The app also runs this SQL on startup when `SQL_INIT_MODE=always`, but running it once manually makes the first deploy easier to debug.
