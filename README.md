# MediSure Microservices Project

MediSure is split into three services with clear ownership:

1. **Hospital Service** (`9091`)
   - Manages patient APIs only (`/api/hospital/patients/*`).
   - Uses `hospital_db`.

2. **Insurance Service** (`9092`)
   - Manages insurance policy APIs and insurance validity checks.
   - Uses `insurance_db`.

3. **Claims Service** (`9093`)
   - Owns all claim creation, claim updates, and claim retrieval APIs.
   - Uses `claims_db`.
   - Integrates with hospital and insurance services.

## Database Connection

Each service now has its own MySQL database:

- `hospital_db`
- `insurance_db`
- `claims_db`

Set credentials in each service's `src/main/resources/application.properties`.

## Getting Started

Run each service in separate terminals:

```bash
cd hospital-service
mvn clean install
mvn spring-boot:run
```

```bash
cd insurance-service
mvn clean install
mvn spring-boot:run
```

```bash
cd claims-service
mvn clean install
mvn spring-boot:run
```# Medisure
