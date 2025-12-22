# BGV Billing & Entitlement Service

This project is a Spring Boot application that provides a billing and entitlement service for a BGV verification SaaS.

## Project Structure

- `pom.xml`: Maven project file with all dependencies.
- `src/main/java/com/bgv/billing/service`: Main source code.
  - `BgvBillingServiceApplication.java`: Main Spring Boot application class.
  - `controller`: REST controllers for the public, user, and admin APIs.
  - `dto`: Data Transfer Objects for API requests and responses.
  - `entity`: JPA entities for the database tables.
  - `enums`: Enum types used in the application.
  - `repository`: Spring Data JPA repositories for database access.
  - `service`: Service interfaces and implementations for the business logic.
- `src/main/resources`: Application resources.
  - `application.properties`: Configuration for the database, JPA, and Flyway.
  - `db/migration`: Flyway database migration files.
    - `V1__create_billing_tables.sql`: DDL for the database schema.
    - `V2__seed_plans_and_features.sql`: Seed data for the `plan` and `feature` tables.

## How to Run

1.  **Set up PostgreSQL:**
    - Create a database named `billing`.
    - Update the `spring.datasource.username` and `spring.datasource.password` in `application.properties` with your database credentials.
2.  **Run the application:**
    - You can run the application from your IDE by running the `main` method in `BgvBillingServiceApplication.java`.
    - Alternatively, you can build the project using Maven and run the JAR file:
      ```bash
      mvn clean install
      java -jar target/bgv-billing-service-0.0.1-SNAPSHOT.jar
      ```
      The application will start on port 8080.

## Next Steps

The skeleton of the application is in place. The next steps are to implement the business logic in the service implementation classes located in the `com.bgv.billing.service.service.impl` package.
