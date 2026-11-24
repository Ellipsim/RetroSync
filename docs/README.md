# RetroSync

**RetroSync** is a web application designed to manage and store video game save files. Users can register, log in, upload their save files, browse them, download them and delete them through a web interface.

The project is developed as part of a university Cloud Native development assignment, focusing on microservices architecture, containerization, service communication, persistent storage and continuous integration.

## Features

- User registration and login.
    
- Password hashing using BCrypt.
    
- Upload of game save files.
    
- Listing saved files associated with a user.
    
- Downloading save files.
    
- Deleting save files.
    
- Persistent storage of user data, save metadata and uploaded files.
    
- Containerized deployment using Docker Compose.
    
- Automated build and verification through GitHub Actions.
    

## Architecture

RetroSync follows a microservices architecture composed of three backend services and a frontend application.

|Component|Technology|Responsibility|Port|
|---|---|---|---|
|Frontend|React, TypeScript, Vite, Nginx|User interface and interaction|3000|
|API Gateway|Java 21, Spring Boot|API routing and proxying requests|8080|
|User Service|Java 21, Spring Boot|Registration and login|8081|
|Save Service|Java 21, Spring Boot|Save file management|8082|
|Database|PostgreSQL 17|Persistent relational data|5432|

The API Gateway acts as the entry point for backend requests and forwards them to the appropriate microservice.

The frontend communicates with the backend through the API Gateway, while the backend services connect to PostgreSQL using separate databases.

### Database separation

PostgreSQL hosts two databases:

- `retrosync_users`: user information and authentication-related data.
    
- `retrosync_saves`: save file metadata.
    

Save files themselves are stored on disk using a Docker persistent volume, rather than directly inside the relational database.

## Technology Stack

### Frontend

- React
    
- TypeScript
    
- Vite
    
- React Router
    
- Nginx
    

### Backend

- Java 21
    
- Spring Boot
    
- Spring Data JPA
    
- Hibernate
    
- Maven
    
- BCrypt
    

### Infrastructure and DevOps

- Docker
    
- Docker Compose
    
- PostgreSQL 17
    
- GitHub Actions
    

## Project Structure

```
RetroSync/
├── .github/
│   └── workflows/
│       └── ci.yml
├── frontend/
│   ├── src/
│   ├── Dockerfile
│   └── nginx.conf
├── services/
│   ├── api-gateway/
│   │   ├── src/
│   │   └── Dockerfile
│   ├── user-service/
│   │   ├── src/
│   │   └── Dockerfile
│   └── save-service/
│       ├── src/
│       └── Dockerfile
├── infrastructure/
│   └── postgres/
│       └── init.sql
├── .env.example
├── compose.yaml
└── README.md
```

## Requirements

To run RetroSync locally, you need:

- Docker Desktop
    
- Docker Compose

## Environment Configuration

The application uses environment variables to configure PostgreSQL.

Create a `.env` file in the root directory based on `.env.example`.

Example:

```
POSTGRES_USER=retrosync
POSTGRES_PASSWORD=retrosync_dev
POSTGRES_PORT=5432
```

Change the credentials as appropriate for your environment.

The `.env` file is excluded from version control and should never be committed.

## Running the Application

Clone the repository:

```
git clone https://github.com/Ellipsim/RetroSync.git
cd RetroSync
```

Create your environment configuration:

```
cp .env.example .env
```

On Windows PowerShell, use:

```
Copy-Item .env.example .env
```

Build and start all services:

```
docker compose up --build -d
```

Docker Compose builds the images and starts the containers in the appropriate order, waiting for PostgreSQL to become healthy before starting the backend services that depend on it.

### Accessing RetroSync

Once the containers are running, open:

**Frontend:** [http://localhost:3000](http://localhost:3000)

Backend entry point:

**API Gateway:** [http://localhost:8080](http://localhost:8080)

The individual backend microservices communicate through the internal Docker Compose network. The user-service and save-service ports are not published to the host in the default configuration.

### Stopping the application

To stop the application:

```
docker compose down
```

This stops and removes the containers and the default network, but preserves named volumes.

## API Overview

Backend requests are routed through the API Gateway.

### User Service

|Method|Endpoint|Description|
|---|---|---|
|POST|`/api/users/register`|Register a new user|
|POST|`/api/users/login`|Verify user credentials|

### Save Service

|Method|Endpoint|Description|
|---|---|---|
|POST|`/api/saves/{userId}`|Upload a save file|
|GET|`/api/saves/{userId}`|List a user's save files|
|GET|`/api/saves/{userId}/{saveId}/download`|Download a save file|
|DELETE|`/api/saves/{userId}/{saveId}`|Delete a save file|

Save uploads use multipart form data.

## Persistence

RetroSync uses Docker named volumes to preserve data independently of the lifecycle of individual containers.

|Volume|Purpose|
|---|---|
|`postgres_data`|PostgreSQL database files|
|`save_files`|Uploaded save files|

Save metadata is stored in PostgreSQL, while the corresponding files are stored in the filesystem mounted through the `save_files` volume.

This separation allows containers to be stopped, recreated or updated without automatically losing application data.

## Continuous Integration

The project includes a GitHub Actions workflow located at:

```
.github/workflows/ci.yml
```

The workflow is triggered on:

- Push events.
    
- Pull requests.
    

The backend pipeline verifies the three Spring Boot services using Maven.

The frontend pipeline installs dependencies and runs the production build using Node.js.

The backend services are tested in separate jobs using a matrix strategy, allowing them to run independently and in parallel.

## Current Limitations

RetroSync is an academic MVP and is not intended to be a production-ready service.

- Login currently verifies credentials but does not issue JWT tokens.
    
- The frontend uses session storage to maintain a simulated authenticated session.
    
- Save operations use the user ID provided in the request path; ownership is not protected by a production authentication and authorization mechanism.
    
- The services share a PostgreSQL container but use separate databases.
    
- The API Gateway uses a manually implemented proxy based on Spring MVC and `RestClient`.
    

## AI-Assisted Development

ChatGPT has been used as a development support tool throughout the project. Its contributions included providing technical explanations, architectural guidance, troubleshooting assistance, implementation suggestions, support with configuration and testing, and help with documentation preparation, particularly with English translation and proofreading.

The generated suggestions were reviewed and iteratively adapted to the project. AI was used as an assistant for development and learning, rather than as a replacement for understanding the project's architecture and implementation.

## Project Status

RetroSync has a working local Docker Compose environment, containerized backend services and frontend, persistent storage, and an automated CI workflow.

The main Cloud Native packaging and continuous integration objectives of the project have been implemented and validated.

---

**Repository:** [https://github.com/Ellipsim/RetroSync](https://github.com/Ellipsim/RetroSync)