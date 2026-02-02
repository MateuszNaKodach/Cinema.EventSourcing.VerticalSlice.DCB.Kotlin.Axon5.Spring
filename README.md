# Vertical Cinema (Kotlin)

Shows how to use Domain-Driven Design, Event Storming, Event Modeling and Event Sourcing in the small Cinema domain.

MIRO Board with the complete Event Modeling:
[CLICK](https://miro.com/app/board/uXjVJNu8wic=/?share_link_id=289537667245)
(you will receive the password during the workshop)

## 🚀 How to run the project?

### Locally (recommended)

1. Install any distribution of Java. At least version 21 (on MacOS I recommend [SDKMAN](https://sdkman.io/install/) to
   do that) on your machine and Docker.
2. Install [Docker Compose](https://docs.docker.com/compose/install/)
3. Clone the repository
4. Make sure that Docker Daemon is running on your machine executing: `docker ps`. If you don't see output like `Cannot connect to the Docker daemon` you're good to go.
5. Build the project executing: `./gradlew build -x test`
6. Run tests after changes with `./gradlew test`
7. If you want to run the application, execute: `docker compose -f docker-compose.axon.yaml up` and `./gradlew bootRun`

To develop the application is the best to use the Intellij IDEA IDE.
- Ultimate version with 30-days trial: [https://www.jetbrains.com/idea/download/](https://www.jetbrains.com/idea/download/)
- Community version: [https://www.jetbrains.com/idea/download](https://www.jetbrains.com/idea/download)

### Devcontainers (experimental)
[Development Containers](https://containers.dev/) - An open specification for enriching containers with development specific content and settings.
If you don't want to install anything on your machine, you can use the devcontainer from the `.devcontainer` directory.
But it gives you slower build/test times than running the project locally.

How to run devcontainer? It depends on your IDE:
- GitHub Codespaces - just go to the repository page, click the `Code` button, select `Codespaces` tab and run new environment.
- IntelliJ IDEA - open the project in IntelliJ IDEA, right click on `.devcontainer/devcontainer.json` and select `Dev Containers` -> `Create Dev Container and Clone Sources`
- Visual Studio Code ....

## 🌐 Interacting with the Application

### REST API
Access the REST API documentation at: [http://localhost:3883/swagger-ui/index.html](http://localhost:3773/swagger-ui/index.html)
