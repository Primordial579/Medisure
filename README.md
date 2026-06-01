# MediSure

MediSure is a full-stack microservices-based Health Insurance Management System developed using Java, Spring Boot, Angular, and modern authentication mechanisms. The platform is designed to manage secure user access, insurance workflows, distributed backend services, and REST-based communication between components.

## Features

* Microservices-based backend architecture
* RESTful API communication using HTTP methods
* Secure authentication using JWT
* Password hashing using Argon2
* Angular frontend for responsive user interaction
* API documentation using Swagger/OpenAPI
* Centralized service communication
* Database integration with ORM support
* Modular and scalable backend structure
* Token validation and authorization workflows

## Tech Stack

### Backend

* Java
* Spring Boot
* Spring MVC
* Spring Security
* Hibernate / JPA
* JDBC
* Microservices Architecture

### Frontend

* Angular
* TypeScript
* HTML
* CSS

### Security

* JWT Authentication
* Argon2 Password Hashing

### Documentation & Tools

* Swagger / OpenAPI
* Maven
* Git & GitHub
* Spring Tool Suite

## Architecture

The application follows a distributed microservices architecture where services communicate using REST APIs. Each module is independently structured for scalability and maintainability.

Main modules include:

* Authentication Service
* User Management Service
* Insurance Service
* API Gateway
* Frontend Client

## API Features

* User Registration
* Login Authentication
* Token Generation & Validation
* Insurance Policy Management
* Protected Endpoints
* CRUD Operations
* Centralized Request Routing

## Security Highlights

* Passwords are securely hashed using Argon2
* JWT-based authorization for protected APIs
* Secure request validation
* Role-based access handling

## Project Goals

* Build a scalable healthcare insurance platform
* Implement secure authentication workflows
* Practice enterprise backend architecture
* Integrate frontend and backend seamlessly
* Deploy distributed applications efficiently

## Installation

### Clone Repository

```bash
git clone https://github.com/Primordial579/Medisure.git
cd Medisure
```

### Backend Setup

```bash
mvn clean install
mvn spring-boot:run
```

### Frontend Setup

```bash
cd frontend
npm install
ng serve
```

## Future Improvements

* Docker containerization
* Kubernetes orchestration
* Payment gateway integration
* Email notification services
* CI/CD pipeline integration
* Advanced analytics dashboard

## Author

Developed by Arjav C Prabhu
