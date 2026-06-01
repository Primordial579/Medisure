# MediSure Hospital Service

The Hospital Service is a microservice that manages patient records for the MediSure insurance claim system.

## Features

- Create patient records
- Retrieve patient by patient ID
- List all patients
- Update patient details

## Technologies Used

- Spring Boot
- Spring Data JPA
- MySQL
- Maven

## Database Configuration

The service connects to a MySQL database. Ensure that the database is set up and the connection details are configured in the `src/main/resources/application.properties` file.

## Getting Started

1. Clone the repository:
   ```
   git clone <repository-url>
   ```

2. Navigate to the hospital-service directory:
   ```
   cd medisure-microservices/hospital-service
   ```

3. Update the database connection settings in `src/main/resources/application.properties`.

4. Run the application:
   ```
   mvn spring-boot:run
   ```

5. Access the API at `http://localhost:9091/api/hospital`.

## MySQL Quick Check

- Ensure MySQL is running on `localhost:3306`.
- The service uses database `hospital_db` and can create it automatically.
- Default credentials come from `src/main/resources/application.properties`:
   - `MYSQL_USER` (default: `root`)
   - `MYSQL_PASSWORD` (default: `root@39`)
   - `MYSQL_HOST` (default: `localhost`)
   - `MYSQL_PORT` (default: `3306`)
   - `MYSQL_DATABASE` (default: `hospital_db`)

Example (PowerShell):

```powershell
$env:MYSQL_USER="root"
$env:MYSQL_PASSWORD="your_password"
mvn spring-boot:run
```

## API Endpoints

- `POST /api/hospital/patients`: Create a patient
- `GET /api/hospital/patients/{patientId}`: Retrieve a patient by ID
- `GET /api/hospital/patients`: Retrieve all patients
- `PUT /api/hospital/patients/{patientId}`: Update patient details

## License

This project is licensed under the MIT License. See the LICENSE file for details.