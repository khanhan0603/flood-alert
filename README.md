# Flood Alert & Rescue Support System - Backend

## Overview

Backend system for flood-risk monitoring, early warning, and emergency
rescue support. The system integrates meteorological data, IoT observations,
machine-learning predictions, geographical data, and rescue coordination
workflows to support four user roles:

- **Administrator**
- **Provincial Coordinator**
- **Rescue Team**
- **Citizen**

The backend is developed with **Java 21 and Spring Boot**, with a separate
**FastAPI AI service** for extreme-rainfall event prediction.

The system uses scheduled workflows for weather-data retrieval, AI prediction,
IoT water-level aggregation, flood-risk assessment, notifications, and
historical-data cleanup rather than continuous real-time AI prediction.

---

## My Role

**Backend Developer**

Responsible for the backend implementation, including:

- REST API development
- Business logic
- Database integration
- Authentication and role-based authorization
- Area and rescue-team management
- Weather-data integration
- IoT data processing
- Flood-risk assessment
- Citizen alerting and notifications
- SOS and hotline rescue workflows
- Rescue coordination and dispatch
- Service-to-service integration with the FastAPI AI service
- Scheduled processing and data cleanup
- REST API testing with Postman
- Deployment configuration

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
|  SOS / Hotline Rescue Requests                 |
|  Rescue Coordination & Dispatch                |
|  Notifications                                 |
|  Scheduled Processing                          |
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

---

### Area & Rescue Team Management

- Manage geographical areas and their boundary information.
- Manage rescue teams and their assigned areas.
- Identify the responsible area from a provided location.
- Identify the responsible rescue team based on the affected area.
- Support location-based rescue coordination.

---

### Initial Area Data Setup

Before running the application for the first time, the required geographical
boundary data must be prepared and placed in the application resources.

The backend expects the following files:

```text
src/main/resources/data/
├── province_boundary_wkt.csv
└── ward_boundary_wkt_full_34.csv
```

These files are used to initialize province and ward geographical data in
the database.

On application startup, the backend checks whether the required ward data
has already been initialized. If the data has not been initialized, the
application imports the province and ward boundary data from the CSV files.

The initialization process:

1. Imports province-level boundary data from
   `province_boundary_wkt.csv`.
2. Imports ward-level boundary data from
   `ward_boundary_wkt_full_34.csv`.
3. Parses WKT geometry using **JTS**.
4. Stores geographical boundaries as spatial data with **SRID 4326**.
5. Establishes the parent-child relationship between provinces and wards.
6. Caches province records during ward import to reduce repeated database
   queries.
7. Saves ward records in batches of 50.

The imported geographical data supports:

- Area identification
- Location-based rescue-team assignment
- Geographical rescue coordination
- Flood-risk workflows

> **Note:** The geographical boundary CSV files are not included in this
> repository. Users must prepare the required files before running the
> application for the first time.

---

### Initial User Data Setup

On application startup, the backend checks whether default users with the
`ADMIN` and `RESCUER` roles already exist.

If the corresponding role does not exist, the backend automatically creates
the default account.

The current initialization creates:

- **1 Administrator account**
- **1 Rescue Team account**

The default accounts are associated with predefined geographical areas:

- Administrator: **Khánh Hòa**
- Rescue Team: **TP. Hồ Chí Minh**

The backend first looks up these areas from the database before creating the
accounts.

#### Default Accounts

| Role | Name | Area | Email | Default Password |
|---|---|---|---|---|
| Administrator | Nguyễn Tí | Khánh Hòa | `ti@gmail.com` | `123456` |
| Rescue Team | Thị Sửu | TP. Hồ Chí Minh | `suu@gmail.com` | `123456` |

Passwords are encoded using the configured `PasswordEncoder` before being
stored in the database.

The initialization process prevents duplicate default accounts by checking
whether an account with the corresponding role already exists.

> **Important:** The default credentials are provided for development and
> testing purposes. Change the default password before using the system in
> a production environment.

The user initialization requires the corresponding geographical areas to
already exist in the database. Therefore, the initial area-data setup must
be completed before the default user accounts can be initialized.

---

### Weather Data Integration

The backend retrieves weather data from **Open-Meteo**.

Weather data is retrieved twice daily:

- **00:30**
- **12:30**

The retrieved weather data is processed and prepared as input for the AI
prediction workflow and flood-risk assessment.

---

### AI Prediction Integration

The Spring Boot backend communicates with a separate **FastAPI AI service**
to request extreme-rainfall event predictions.

The integration uses:

- `RestTemplate`
- Service health checks
- Prediction requests
- Prediction status handling
- Prediction result retrieval
- REST-based service-to-service communication

AI prediction requests are scheduled twice daily:

- **06:30**
- **18:30**

The FastAPI service runs one of three separately trained XGBoost models:

- **1-day ahead**
- **2-day ahead**
- **3-day ahead**

The returned prediction results are consumed by the Spring Boot backend and
used as an input to the flood-risk assessment workflow.

The AI service predicts **extreme-rainfall events**. The Spring Boot backend
subsequently combines these prediction results with IoT observations to
perform flood-risk assessment.

---

### IoT Monitoring & Water-Level Aggregation

The backend receives and monitors IoT water-level observations.

The IoT processing workflow:

- Receives water-level observations.
- Processes incoming IoT measurements.
- Aggregates water-level data **once every minute**.
- Stores and processes aggregated IoT data for use by the risk-assessment
  workflow.

---

### Flood-Risk Assessment

Flood-risk assessment is performed **once every 2 minutes**.

The assessment combines:

- AI extreme-rainfall prediction results
- Aggregated IoT water-level data

The resulting risk assessment supports:

- Flood-risk monitoring
- Citizen warnings
- Emergency response
- Rescue coordination

The AI prediction itself is not continuously executed. Instead, the latest
scheduled AI prediction result is used together with the latest aggregated
IoT data during the risk-assessment workflow.

---

### Citizen Alerts & Notifications

The backend supports notification workflows for relevant system users.

These include:

- Citizen flood-risk alerts
- Notifications related to rescue requests
- Notifications to assigned rescue teams
- Notifications associated with rescue coordination and dispatch

---

### SOS & Hotline Rescue Support

Citizens can request emergency rescue support through the system's SOS and
**rescue hotline** workflow.

The rescue workflow includes:

1. Citizen submits a rescue request or contacts the rescue hotline.
2. The system receives the required rescue information and location.
3. The system identifies the affected area from the provided location.
4. The responsible rescue team is determined.
5. Environmental and victim conditions are evaluated.
6. Rescue priority is calculated.
7. The request is coordinated and dispatched to the responsible rescue team.
8. Notifications are sent to the assigned rescue team.

The workflow supports location-based assignment and prioritization of
emergency rescue requests.

---

## Scheduled System Workflows

The backend uses scheduled jobs for recurring system operations rather than
continuous real-time AI prediction.

| Schedule | Operation |
|---|---|
| **00:30 daily** | Retrieve weather data from Open-Meteo |
| **06:30 daily** | Call FastAPI AI service for prediction |
| **12:30 daily** | Retrieve weather data from Open-Meteo |
| **18:30 daily** | Call FastAPI AI service for prediction |
| **Every 1 minute** | Aggregate IoT water-level observations |
| **Every 2 minutes** | Assess flood risk using AI prediction + aggregated IoT data |
| **23:50 daily** | Delete data older than 8 days |

### Scheduled Workflow

```text
00:30 / 12:30
      |
      v
Retrieve weather data from Open-Meteo
      |
      v
Process / prepare weather data
      |
      v
06:30 / 18:30
      |
      v
Spring Boot calls FastAPI
      |
      v
XGBoost prediction
      |
      v
Store prediction result
      |
      +--------------------------------+
                                       |
IoT observations                       |
      |                                |
      v                                |
Every 1 minute                         |
      |                                |
      v                                |
Aggregate water-level data             |
      |                                |
      +----------------+---------------+
                       |
                       v
                Every 2 minutes
                       |
                       v
             Flood-Risk Assessment
                       |
             +---------+---------+
             |                   |
             v                   v
       AI Prediction       Aggregated IoT
          Results           Water-Level Data
             |                   |
             +---------+---------+
                       |
                       v
                 Risk Assessment
                       |
                       v
              Alert / Notification


23:50 daily
      |
      v
Delete data older than 8 days
```

---

## AI Prediction Service

The machine-learning prediction functionality is provided by a separate
FastAPI service.

The service contains three XGBoost models for extreme-rainfall event
prediction:

- **1-day ahead**
- **2-day ahead**
- **3-day ahead**

The models are trained separately in the AI training repository and loaded
by the FastAPI service for inference.

The Spring Boot backend calls the FastAPI service at **06:30 and 18:30 daily**.

The prediction results are then used together with aggregated IoT water-level
data during the flood-risk assessment workflow.

For details about historical data processing, feature engineering, target
definition, model training, evaluation, and threshold selection, see the
AI training repository.

---

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Data JPA
- REST API
- PostgreSQL
- JTS / spatial data processing

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

### Spring Boot Backend

Spring Boot REST API endpoints are tested using **Postman**.

Testing covers the implemented REST API workflows and backend service
interactions.

### FastAPI AI Service

FastAPI prediction endpoints can be tested through the automatically
generated **Swagger UI / OpenAPI documentation**.

Local Swagger UI:

```text
http://localhost:8000/docs
```

OpenAPI schema:

```text
http://localhost:8000/openapi.json
```

---

## CI/CD

### Spring Boot Backend

GitHub Actions is used for automated CI validation of the Spring Boot
backend.

The workflow includes:

- Java 21 environment setup
- PostgreSQL service setup
- Maven build validation
- Validation on pushes and pull requests

### FastAPI AI Service

GitHub Actions is used for CI/CD of the FastAPI service.

The workflow includes:

- Automated application validation
- Deployment trigger for the `main` branch
- Deployment to Render

### CI/CD Workflow

```text
Push / Pull Request
        |
        v
GitHub Actions
        |
        v
Automated Validation
        |
        +---- Pull Request --> CI validation
        |
        +---- Push to main --> Trigger Render Deployment
```

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

### 1. Prepare Geographical Data

Before starting the application for the first time, prepare the required
geographical boundary files and place them under:

```text
src/main/resources/data/
├── province_boundary_wkt.csv
└── ward_boundary_wkt_full_34.csv
```

The files must contain the required province and ward boundary data in the
expected CSV/WKT format.

### 2. Run the Application

```bash
mvn spring-boot:run
```

On startup, the application checks the database and imports the required
geographical data when it has not already been initialized.

---

## Health Check

The backend provides a lightweight health-check endpoint:

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
│           └── data/
│               ├── province_boundary_wkt.csv
│               └── ward_boundary_wkt_full_34.csv
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

https://github.com/khanhan0603/ai-server-flood-alert

### AI Training & Model Development

Repository containing the historical meteorological data processing,
feature engineering, target generation, XGBoost model training, and model
evaluation workflow.

https://github.com/khanhan0603/flood-alert-ai

### Research Reference

The AI training workflow inherits research ideas and data-processing concepts
from the open-source **ECMWF Code for Earth `ml_flood`** project.

https://github.com/ECMWFCode4Earth/ml_flood

---

## Project Context

This Spring Boot backend is the main application backend of the
**Flood Alert & Rescue Support System**.

It coordinates:

- Weather-data retrieval
- AI prediction requests
- IoT data processing
- Flood-risk assessment
- Citizen alerts
- SOS and hotline rescue requests
- Rescue coordination and dispatch
- Notifications
- Scheduled data maintenance

The machine-learning prediction service is separated into FastAPI so that
model inference can be provided as an independent service.

The system does **not** perform continuous real-time AI prediction. Weather
retrieval, AI prediction, IoT aggregation, flood-risk assessment, and
historical-data cleanup are executed according to their respective schedules.

The flood-risk assessment combines the latest available AI prediction result
with aggregated IoT water-level data to support warning and emergency-response
decisions.

---

## Project Status

The backend implements the core flood monitoring, risk assessment,
notification, and emergency rescue workflows described above.
