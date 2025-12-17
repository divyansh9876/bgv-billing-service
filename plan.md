# **Enhanced Technical Documentation: Merchant Self-Registration Service**

*Version 1.1*

## **1. Introduction and Scope**

### **1.1. Purpose**
This document provides a comprehensive technical deep-dive into the `merchant-self-registration-service`. This Java Spring Boot microservice is the authoritative system for orchestrating the end-to-end, multi-step onboarding process for new merchants into the "Boost" ecosystem. It functions as the secure and stateful backend engine for any user-facing frontend application (e.g., web portal, mobile app).

### **1.2. Scope**
The service's responsibilities include:
-   Exposing a secure REST API for the entire registration lifecycle.
-   Persisting and managing the state of a merchant's application, allowing them to pause and resume.
-   Collecting and validating all required merchant data, from business details to banking information.
-   Handling the secure upload of identity and business documents.
-   Integrating with external systems for OCR processing and upstream authentication.
-   Providing consistent, structured error responses.

---

## **2. System Architecture**

The service employs a classic **Layered "Hexagonal" Architecture**. This strictly separates concerns, enhances testability, and isolates the core business logic from external interfaces (like the web controllers or database).

```mermaid
graph TD
    subgraph "External Interfaces"
        A[Frontend Client]
        B[External Systems]
    end

    subgraph "Application Core"
        subgraph "Adapter Layer (Ports & Adapters)"
            C[API Controllers<br>(controller package)]
            D[External Service Clients<br>(service/impl)]
        end

        subgraph "Business Logic (Domain)"
            E[Service Layer<br>(service package)]
        end

        subgraph "Persistence Layer (Adapters)"
            F[JPA Repositories<br>(repository package)]
            G[Database<br>(PostgreSQL)]
        end
    end

    A -- REST API Calls --> C
    C -- Calls --> E
    E -- Uses --> F
    F -- Interacts with --> G
    E -- Calls --> D
    D -- Interacts with --> B
```

-   **Controllers (`controller`):** The primary "inbound adapter" for HTTP requests. It handles endpoint routing, deserializes JSON from DTOs, performs initial validation, and delegates all business operations to the Service Layer. It is intentionally kept "thin".
-   **Services (`service`):** The heart of the application's business logic. It orchestrates complex operations, manages transactional integrity (`@Transactional`), and contains the state machine logic for the registration flow. It is the central point of coordination.
-   **Repositories (`repository`):** A "outbound adapter" for data persistence. These Spring Data JPA interfaces abstract away the underlying database operations, providing a clean, high-level API for data access (e.g., `merchantRepository.save(merchant)`).
-   **Entities (`entity`):** Defines the core domain model and its mapping to the PostgreSQL database schema via JPA annotations (`@Entity`, `@Table`, etc.).
-   **DTOs (`dto`):** Data Transfer Objects define the public contract of the API. They are essential for decoupling the API schema from the internal database model, preventing unintended exposure of database fields and providing versioning flexibility.

---

## **3. Technology Stack**

| Category                  | Technology / Library          | Purpose                                                                                           |
| ------------------------- | ----------------------------- | ------------------------------------------------------------------------------------------------- |
| **Core Framework**        | Spring Boot                   | Provides the foundation for dependency injection, web server, configuration, and security.        |
| **Language**              | Java                          | Core programming language for the application.                                                    |
| **Build Tool**            | Apache Maven                  | Manages project dependencies, build lifecycle, and packaging.                                     |
| **Database**              | PostgreSQL                    | The relational database used for persisting all merchant data.                                    |
| **DB Migrations**         | Flyway                        | Version-controls the database schema, ensuring consistent state across all environments.          |
| **Data Access**           | Spring Data JPA / Hibernate   | Implements the repository pattern and maps Java objects to database tables (ORM).                 |
| **File Storage**          | AWS SDK for Java (S3)         | Provides the client for securely uploading, storing, and retrieving merchant documents from AWS S3. |
| **Text Extraction**       | Tess4J (Tesseract)            | An OCR library used to extract text from uploaded document images for verification/pre-filling.   |
| **API Security**          | Spring Security               | Core framework for authentication and authorization.                                              |
| **API Documentation**     | Swagger / Springfox           | Generates interactive API documentation from code annotations (`SwaggerConfig.java`).             |

---

## **4. Core Process: Merchant Registration State Machine**

The registration is a strictly ordered state machine managed by `RegistrationServiceImpl`. A merchant's progress is tracked by the `stepsCompleted` field in the `Merchant` entity. A user cannot proceed to step `N+1` before successfully completing step `N`.

| Step | State (`stepsCompleted`) | Endpoint(s)                          | DTOs Involved        | Description                                                                                                                             |
| :--: | :----------------------: | ------------------------------------ | -------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| 0    | `0`                      | `POST /register`                     | `RegisterRequestDTO` | Initiates the flow. Creates a `Merchant` record linked to a Boost user ID. Sets state to 0.                                           |
| 1    | `1`                      | `POST /step-one`                     | `StepOneDTO`         | Submits core business details (name, category, etc.). On success, updates state to 1.                                                 |
| 2    | `2`                      | `POST /step-two`                     | `StepTwoDTO`         | Submits banking and payout information. On success, updates state to 2.                                                               |
| 3    | `3`                      | `POST /step-three`                   | `StepThreeDTO`       | Submits business address details. On success, updates state to 3.                                                                       |
| 4    | `4`                      | `GET /confirm-details`<br>`POST /confirm-details` | `ConfirmDetailsDTO`  | `GET` retrieves all data for final review. `POST` confirms the application, marks it as complete, and likely triggers downstream processing. |

### **Sequence Diagram: Step 1 Example**
```mermaid
sequenceDiagram
    participant Client as Frontend Client
    participant Controller as MerchantRegistrationController
    participant Service as RegistrationServiceImpl
    participant Repo as MerchantRepository
    participant DB as Database

    Client->>+Controller: POST /step-one (headers, StepOneDTO)
    Controller->>+Service: finishStep1(merchantId, stepOneData)
    Service->>+Repo: findById(merchantId)
    Repo-->>-Service: Merchant(stepsCompleted=0)
    Service->>Service: Validate(merchant.stepsCompleted == 0)
    Service->>Service: Process & Save Business Details
    Service->>+Repo: save(merchant.setStepsCompleted(1))
    Repo-->>-DB: UPDATE merchants SET steps_completed = 1 WHERE...
    DB-->>-Repo: Success
    Repo-->>-Service: Updated Merchant
    Service-->>-Controller: Success Response
    Controller-->>-Client: 200 OK (BaseResponse)
```

---

## **5. API Endpoints Details**

### **5.1. MerchantRegistrationController**
| Method | Path                  | Request DTO           | Response DTO        | Security                                     | Description                                                 |
| :----- | :-------------------- | :-------------------- | :------------------ | :------------------------------------------- | :---------------------------------------------------------- |
| `POST` | `/register`           | `RegisterRequestDTO`  | `BaseResponse`      | HMAC Auth                                    | Kicks off the registration for a given Boost user.          |
| `POST` | `/step-one`           | `StepOneDTO`          | `BaseResponse`      | Registration-based (must be on correct step) | Submits and saves business details.                         |
| `POST` | `/step-two`           | `StepTwoDTO`          | `BaseResponse`      | Registration-based                               | Submits and saves bank/payout details.                      |
| `POST` | `/step-three`         | `StepThreeDTO`        | `BaseResponse`      | Registration-based                               | Submits and saves address details.                          |
| `GET`  | `/confirm-details`    | -                     | `ConfirmDetailsDTO` | Registration-based                               | Retrieves all submitted data for the final review screen.   |
| `POST` | `/confirm-details`    | -                     | `BaseResponse`      | Registration-based                               | Confirms the application and finalizes the registration.    |

### **5.2. FileUploadController**
| Method | Path                  | Request DTO           | Response DTO        | Security           | Description                                                        |
| :----- | :-------------------- | :-------------------- | :------------------ | :----------------- | :----------------------------------------------------------------- |
| `POST` | `/upload-document`    | `MultipartFile`       | `DocumentResponseDTO` | Registration-based | Uploads a document, saves it to S3, and returns the document ID/URL. |
| `POST` | `/ocr`                | `DocumentDTO`         | `OcrResponseDTO`    | Registration-based | Triggers OCR processing on a previously uploaded document.         |

### **5.3. OtpAuthController**
| Method | Path                  | Request DTO           | Response DTO        | Security           | Description                                                                 |
| :----- | :-------------------- | :-------------------- | :------------------ | :----------------- | :-------------------------------------------------------------------------- |
| `POST` | `/generate-otp`       | `(inferred)`          | `BaseResponse`      | HMAC Auth          | Generates and sends an OTP to the merchant's contact (email/phone).         |
| `POST` | `/validate-otp`       | `(inferred)`          | `BaseResponse`      | HMAC Auth          | Validates the OTP provided by the user, granting access to a protected action. |

---

## **6. Database Model and Entity Relationships**

The database schema is managed by Flyway scripts located in `src/main/resources/db/migration`. The core domain is modeled with JPA entities.

```mermaid
erDiagram
    MERCHANT {
        bigint id PK
        integer stepsCompleted
        string boostUserId
    }
    BUSINESS {
        bigint id PK
        bigint merchant_id FK
        string businessName
        string businessType
    }
    PAYOUT_DETAILS {
        bigint id PK
        bigint merchant_id FK
        string bankAccountNumber
        string bankCode
    }
    ADDRESS {
        bigint id PK
        string street
        string city
        string postcode
    }
    DOCUMENT {
        bigint id PK
        bigint merchant_id FK
        string documentType
        string s3Path
    }
    MERCHANT ||--o{ BUSINESS : "has"
    MERCHANT ||--o{ PAYOUT_DETAILS : "has"
    BUSINESS ||--o{ ADDRESS : "has"
    MERCHANT ||--o{ DOCUMENT : "has"
```

-   **`Merchant`**: The aggregate root. It holds the primary identifier and the crucial `stepsCompleted` state field. It has one-to-one or one-to-many relationships with all other core data entities.
-   **`Business`**: Stores details specific to the merchant's company. Linked via a foreign key to `Merchant`.
-   **`PayoutDetails`**: Contains sensitive banking information for settlements. Linked via a foreign key to `Merchant`.
-   **`Document`**: A record for each uploaded file, containing metadata like the type of document and its location (path) in the S3 bucket.

---

## **7. Deeper Dive: Security Implementation**

-   **`HmacAuthFilter.java`**: This custom `OncePerRequestFilter` intercepts incoming requests to protected endpoints.
    1.  It extracts specific headers (e.g., `X-Signature`, `X-Timestamp`, `Authorization`).
    2.  It reconstructs the message to be signed from parts of the request (e.g., method, path, timestamp, request body).
    3.  It retrieves the client's secret key (likely based on the client ID in the `Authorization` header).
    4.  It computes its own HMAC-SHA256/512 signature of the reconstructed message.
    5.  It securely compares its computed signature with the one from the `X-Signature` header. If they don't match or the timestamp is too old, it rejects the request with a `401 Unauthorized` or `403 Forbidden` error.

-   **`OtpBasedSecurityConfig.java`**: Configures a separate filter chain for endpoints related to OTP generation and validation. This flow is critical for verifying user identity for sensitive actions. The `OtpService` handles the logic of generating a random code, storing it with an expiry in the `Otp` table, and later validating it.

-   **`RegistrationBasedSecurityConfig.java`**: This is the most application-specific security configuration. It likely includes a custom `AuthorizationManager` or filter that:
    1.  Extracts the `merchantId` from the request path or token.
    2.  Loads the `Merchant` entity from the database.
    3.  Checks the `stepsCompleted` value against the requirements of the target endpoint (e.g., to access `/step-two`, `stepsCompleted` must be `1`).
    4.  If the check fails, it denies access, preventing users from skipping steps.

---

## **8. Configuration and Environment Variables**

The service is configured via `src/main/resources/application.yml`. For local development and deployment, the following properties are critical and should be managed via environment variables or a secure configuration server.

| Property Path                         | Example Value                 | Description                                                  |
| ------------------------------------- | ----------------------------- | ------------------------------------------------------------ |
| `spring.datasource.url`               | `jdbc:postgresql://localhost:5432/merchantdb` | The JDBC URL for the PostgreSQL database.                    |
| `spring.datasource.username`          | `postgres`                    | Username for the database connection.                        |
| `spring.datasource.password`          | `your-secret-password`        | Password for the database connection.                        |
| `aws.s3.access-key`                   | `AKIAIOSFODNN7EXAMPLE`        | Your AWS Access Key ID for S3 access.                        |
| `aws.s3.secret-key`                   | `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY` | Your AWS Secret Access Key for S3 access.                    |
| `aws.s3.bucket-name`                  | `merchant-documents-prod`     | The name of the S3 bucket to store uploaded files.           |
| `boost.oauth.url`                     | `https://auth.boost.com/`     | The base URL of the external Boost authentication service.   |
| `hmac.secret.client-abc`              | `long-random-secret-for-client-abc` | An example of a shared secret key for an HMAC client.        |

---

## **9. Global Error Handling**

The `exception` package contains a robust global error handling mechanism.
-   **`GlobalExceptionHandler.java`**: This `@RestControllerAdvice` class catches exceptions thrown from anywhere in the application (controllers, services).
-   **`ErrorCode.java`**: An enum that defines a finite list of specific error conditions (e.g., `INVALID_STEP`, `DOCUMENT_NOT_FOUND`, `OTP_EXPIRED`).
-   **`BusinessException.java`**: A custom runtime exception that takes an `ErrorCode` as a parameter.
-   **Flow**: When a specific error condition occurs in the service layer (e.g., a user tries to skip a step), the code throws a `new BusinessException(ErrorCode.INVALID_STEP)`. The `GlobalExceptionHandler` catches this, looks up the corresponding HTTP status code and message from the `ErrorCode`, and formats a standardized JSON error response (`ApiResponse.java`) to send to the client. This ensures all API errors are consistent, predictable, and don't expose internal stack traces.

---

## **10. Developer Setup Guide**

1.  **Prerequisites:** Install Java, Maven, and Docker (for PostgreSQL).
2.  **Database Setup:**
    ```bash
    docker run --name merchant-db -e POSTGRES_PASSWORD=mysecretpassword -e POSTGRES_DB=merchantdb -p 5432:5432 -d postgres
    ```
3.  **Configuration:**
    - Create a local `application-local.yml` file in `src/main/resources`.
    - Override the necessary properties (datasource, AWS credentials, etc.) in this file. Using a `-local` profile prevents committing secrets to Git.
4.  **Build Project:**
    ```bash
    mvn clean install
    ```
5.  **Run Application:**
    - From your IDE, run `MerchantRegistrationApplication.java` with the `local` Spring profile enabled.
    - Or via command line:
    ```bash
    java -jar -Dspring.profiles.active=local target/merchant-self-registration-service-*.jar
    ```
    On startup, Flyway will automatically connect to the database and apply all migrations found in `src/main/resources/db/migration`. The service will then be available, typically on `http://localhost:8080`.
