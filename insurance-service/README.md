# MediSure Insurance Claim Microservices

## Insurance Service

The Insurance Service is a microservice responsible for managing insurance policies and coverage validation within the MediSure application.

### Features

- **Create Insurance Policy**: Allows users to create new insurance policies.
- **Get Policy by ID**: Retrieve details of a specific insurance policy using its ID.
- **Verify Insurance Validity**: Validates insurance coverage for estimated claim amounts.

### Architecture

The Insurance Service follows a microservices architecture, utilizing Spring Boot for building the application and MySQL for data persistence. The service is designed to be scalable and maintainable, adhering to best practices in software development.

### Database

The service connects to a MySQL database. Ensure that the database is set up and the connection details are correctly configured in the `application.properties` file.

### Getting Started

1. Clone the repository.
2. Navigate to the `insurance-service` directory.
3. Update the `application.properties` file with your MySQL database credentials.
4. Run the application using your preferred method (e.g., through an IDE or command line).

### API Documentation

Refer to the API documentation for detailed information on the available endpoints and their usage.

### License

This project is licensed under the MIT License. See the LICENSE file for more details.