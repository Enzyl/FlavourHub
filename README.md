# FlavourHub
Welcome to FlavourHub! This is a Java-based web application built with Spring Boot and PostgreSQL, designed to provide a platform for ordering food from nearby restaurants.

# Features
* User registration and authentication
* Browse nearby restaurants
* Place and track orders
* User profile management
* Swagger UI for API documentation
  
# Technologies Used
* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* Thymeleaf
* PostgreSQL
* Docker
* Gradle
* Lombok
* MapStruct
* Hibernate
* Flyway
* TestContainers
* Mockito
* WireMock
* RestAssured
* Swagger

  
# Setup and Installation
## Prerequisites
* Java 17
* Docker

## Clone the Repository
* git clone https://github.com/Enzyl/FlavourHub.git
* cd FlavourHub
## Build the Application
* gradlew.bat clean build -x test

## Running with Docker
To start the application using Docker, run the following command:
* docker-compose up -d (if u want to run app with logs, delete "-d")
### This will start both the backend application and the PostgreSQL database.

# Accessing the Application
Once the application is running, you can access it at:
* http://localhost:8190
  
# API Documentation
The API documentation is available via Swagger UI. You can access it at:
* http://localhost:8190/swagger-ui.html
