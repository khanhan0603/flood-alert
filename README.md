# Flood Alert & Rescue Support System - Backend

## Overview

Backend system for flood-risk monitoring, early warning, and emergency
rescue support. The system integrates meteorological data, IoT observations,
machine-learning predictions, and rescue coordination workflows to support
four user roles:

- **Administrator**
- **Provincial Coordinator**
- **Rescue Team**
- **Citizen**

The backend is developed with **Java 21 and Spring Boot**, with a separate
**FastAPI AI service** for extreme-rainfall event prediction.

---

## My Role

**Backend Developer**

Responsible for the backend implementation, including REST API development,
business logic, database integration, service-to-service integration, rescue
workflows, authentication, notifications, and integration with the AI
prediction service and IoT data.

---

## System Architecture

```text
                    Open-Meteo
                        |
                        v
                 Weather Data
                        |
                        v
+------------------------------------------------+
|              Spring Boot Backend               |
|                                                |
|  Authentication & Authorization                |
|  Area & Rescue Team Management                 |
|  Weather Data Management                       |
|  IoT Monitoring                                |
|  Flood-Risk Assessment                         |
|  Citizen Alerting                              |
|  SOS Rescue Requests                           |
|  Rescue Coordination & Dispatch                |
|  Notifications                                 |
+----------------------+-------------------------+
                       |
                       | REST / RestTemplate
                       v
              +-------------------+
              | FastAPI AI Service |
              +-------------------+
                       |
                       v
                XGBoost Models
                |      |      |
              1-day  2-day  3-day
              prediction models
```

---

## Key Features

### User & Access Management

Supports four system roles:

- Administrator
- Provincial Coordinator
- Rescue Team
- Citizen

Provides authentication and role-based access control for protected
application workflows.

### Area & Rescue Team Management

- Manage areas and geographical information.
- Manage rescue teams and their assigned areas.
- Identify the responsible rescue team based on the affected area.

### Weather Data Integration

- Retrieve weather data from **Open-Meteo**.
- Process weather information for flood-risk assessment.
- Use forecast data as input for the AI prediction service.

### AI Prediction Integration

The Spring Boot backend communicates with a separate **FastAPI AI service**
to request extreme-rainfall event predictions.

The integration uses:

- `RestTemplate`
- Service health checks
- Prediction requests
- Prediction status handling
- Prediction result retrieval

### IoT Monitoring & Flood-Risk Assessment

- Receive and monitor IoT observations.
- Combine weather forecast/prediction data with IoT data.
- Assess flood risk based on environmental information.
- Support risk-based warning and emergency response workflows.

### Citizen Alerts & Notifications

- Generate alerts for citizens based on assessed flood risks.
- Handle system notifications for relevant users.
- Notify assigned rescue teams when emergency requests are dispatched.

### SOS Rescue Workflow

The SOS workflow includes:

1. Citizen submits a rescue request.
2. System identifies the affected area from the request location.
3. System determines the responsible rescue team.
4. Environmental and victim conditions are evaluated.
5. Rescue priority is calculated.
6. The request is coordinated and dispatched to the responsible rescue team.
7. Notifications are sent to the assigned rescue team.

---

## AI Prediction Service

The system uses a separate FastAPI service containing **three XGBoost
models** for flood-risk prediction at different lead times:

- **1-day ahead**
- **2-day ahead**
- **3-day ahead**

The models were trained using historical meteorological data with temporal
features and evaluated using:

- ROC-AUC
- Precision
- Recall
- F1-score

The Spring Boot backend consumes the prediction service through REST APIs.

For details about data processing, feature engineering, model training,
and evaluation, see the AI training repository.

---

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Data JPA
- REST API
- PostgreSQL

### AI Service

- Python
- FastAPI
- XGBoost

### External Services

- Open-Meteo API

### Development & Testing

- Maven
- Git
- GitHub Actions
- Docker
- Postman

### Deployment

- Render

---

## Testing

- Tested Spring Boot REST API endpoints using **Postman**.
- Tested FastAPI prediction endpoints through the service's API
  documentation.

---

## CI/CD

### Spring Boot Backend

GitHub Actions is used for automated CI validation of the Spring Boot
backend, including:

- Java 21 environment setup
- PostgreSQL service setup
- Maven build validation
- Validation on pushes and pull requests

### FastAPI AI Service

GitHub Actions is used for CI/CD of the FastAPI service:

- Automated application validation
- Deployment trigger for the `main` branch
- Deployment to Render

---

## Deployment

The Spring Boot backend includes deployment configuration for **Render**.

The repository includes:

- `Dockerfile` for building and running the Spring Boot application with
  Java 21
- `render.yaml` for Render service configuration
- PostgreSQL database configuration
- Render Key Value configuration
- `/health` endpoint for service health checking

### Deploy to Render

1. Push the repository to GitHub.
2. In Render, select **New > Blueprint**.
3. Connect this GitHub repository.
4. Select the branch containing `render.yaml`.
5. Apply the Blueprint.
6. After the PostgreSQL database is created, open the database shell and
   run:

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

The application can also run this SQL automatically when
`SQL_INIT_MODE=always`.

---

## Run Locally

Make sure Java 21, Maven, and PostgreSQL are installed and configured.

Run the application with:

```bash
mvn spring-boot:run
```

---

## Health Check

The backend provides a lightweight health check endpoint:

```text
GET /health
```

This endpoint is used to verify that the deployed service is running
correctly.

---

## Project Structure

```text
flood-alert/
├── .github/
│   └── workflows/
│       └── ci.yml
├── src/
│   └── main/
│       ├── java/
│       └── resources/
├── Dockerfile
├── render.yaml
├── pom.xml
└── README.md
```

---

## Related Repositories

### AI Prediction Service

FastAPI service responsible for serving the trained XGBoost models and
providing prediction endpoints consumed by the Spring Boot backend.

**Repository:**  
https://github.com/khanhan0603/ai-server-flood-alert

### AI Training & Model Development

Repository containing the data processing, feature engineering, model
training, and evaluation workflow for the XGBoost prediction models.

**Repository:**  
https://github.com/khanhan0603/flood-alert-ai

---

## Project Status

The backend implements the core flood monitoring, risk assessment,
notification, and emergency rescue workflows described above.