<table>
<tr>
<td>
  <img src="logoreadme.jpg" alt="Search Engine Project" width="120" height="120">
</td>
<td>
  <h1>Search Engine Project - Stage 3</h1>
</td>
</tr>
</table>

- **Subject:** Big Data (BD)
- **Academic Year:** 2024-2025
- **Degree:** Data Science and Engineering (GCID)
- **School:** School of Computer Engineering (EII)
- **University:** University of Las Palmas de Gran Canaria (ULPGC)

## Project Overview

This project focuses on the development of a robust and scalable search system designed to efficiently manage large volumes of information. Based on a hexagonal architecture, the project ensures a modular and clear organization, allowing for the seamless integration of new components and ensuring adaptability to different technological environments.

This system is designed to optimize performance in high-load scenarios and complex queries, promoting code extensibility and maintainability. Furthermore, the use of modern tools and advanced techniques ensures that the system can meet the most demanding search needs. With a focus on scalability and efficiency, the project aims to provide a versatile and effective solution for data management and query resolution in dynamic environments.

## Main Functionality

- **Modular and scalable management:**
The system is designed with a hexagonal architecture that organizes the code into well-defined layers. This approach facilitates the integration of new components, ensuring high adaptability and maintainability over time.

- **Efficient query mechanism:**
The search engine processes queries using optimized techniques to handle large volumes of data. This ensures fast response times, even in scenarios with high concurrency and complex queries.

- **User Interface (UI):**
The system features an intuitive user interface that allows users to perform queries easily and view results clearly and organized. This interface is designed to enhance the end-user experience.

- **REST API:**
A REST API is implemented to enable programmatic queries and provide statistics related to searches and the system itself. This facilitates integration with other applications and services.

- **Support for distributed deployment:**
Using Docker containerization, the system can be deployed in multiple instances, allowing load balancing and supporting a high number of simultaneous queries. This ensures efficient operation in distributed environments.

- **Data storage and organization:**
Data is managed using advanced structures that optimize storage and information retrieval, improving the overall performance of the system.

- **Performance and scalability testing:**
The system includes detailed tests to evaluate its behavior under different data volumes and concurrency levels. These tests help identify potential bottlenecks and optimize performance.

- **Extensibility:**
Thanks to its modular design, the system can be easily extended to include new features, functionalities, or services, ensuring its long-term usability.

## Project Structure

```
📁 datalake
📁 datamart

📁 src
├── 📁 main
│   ├── 📁 java
│   │   ├── 📁 adapters
│   │   │   ├── 📁 external
│   │   │   ├── 📁 benchmark
│   │   │   ├── 📁 persistence
│   │   │   ├── 📁 queries
│   │   │   └── 📁 rest
│   │   ├── 📁 application
│   │   ├── 📁 config
│   │   ├── 📁 domain
│   │   └── 📁 main
│   └── 📁 resources
│       └── 📁 static
└── 📁 test

Dockerfile
pull.sh
push.sh
pom.xml
```

## Development Environment:
- **IDE:** IntelliJ IDEA.
- **Version Control:** Git & GitHub for source code management and collaboration.
- **Dependency Management:** Maven for dependency management and module building.
- **Containerization:** A single Dockerfile is used for easy deployment and scalability across environments.

## Docker Configuration:
Each module includes a dedicated `Dockerfile`.

```dockerfile
FROM openjdk:22-jdk-slim

WORKDIR /app

COPY target/Definitivo-1.0-SNAPSHOT.jar /app/Definitivo.jar

ENV DATATYPE=text
ENV INVERTED_INDEX_FILE=invertedIndex
ENV METADATA_FILE=metadata

RUN mkdir -p /app && \
  bash -c 'case "$DATATYPE" in \
    text) touch /app/${INVERTED_INDEX_FILE}.txt /app/${METADATA_FILE}.txt ;; \
    binary) touch /app/${INVERTED_INDEX_FILE}.bin /app/${METADATA_FILE}.bin ;; \
    kryo) touch /app/${INVERTED_INDEX_FILE}.kryo /app/${METADATA_FILE}.kryo ;; \
    cbor) touch /app/${INVERTED_INDEX_FILE}.cbor /app/${METADATA_FILE}.cbor ;; \
    avro) touch /app/${INVERTED_INDEX_FILE}.avro /app/${METADATA_FILE}.avro ;; \
    *) echo "Unsupported data type: $DATATYPE" && exit 1 ;; \
  esac'

COPY src/main/resources /app/static

EXPOSE 4567

CMD ["java", "-jar", "Definitivo.jar"]
````

## Running the Application:

1. **Clone the Repository:** Clone the project repository.

2. **Install Dependencies:** Use Maven to install dependencies in the main directory

```bash
  mvn clean package -DskipTests
```

## Running the Docker

To deploy and run the system using Docker, the following scripts are used:

1. **On a single computer (main machine):** Run the following command to build and publish the Docker image:

```bash
  ./push.sh
```

2. **On all computers or nodes (secondary machines):** Run this command to pull the published image from the registry:

```bash
  ./pull.sh
```

These scripts automate the process of creating and distributing the image, ensuring that all nodes are correctly synchronized with the latest system configuration.

## SOLID Principles and Design Patterns
The project follows the five SOLID principles for maintainability and scalability, using design patterns such as the Factory Method to ensure the system is extensible and robust.

- **Single Responsibility Principle (SRP):** Each class has a single, clearly defined responsibility, reducing complexity and facilitating system maintenance.
- **Open/Closed Principle (OCP):** The code is designed to be extensible without requiring modifications to existing implementations, ensuring system stability.
- **Liskov Substitution Principle (LSP):** Derived classes can replace their base classes without altering system functionality, ensuring code consistency and compatibility.
- **Interface Segregation Principle (ISP):** Interfaces are designed to be specific and relevant to the modules that use them, avoiding unnecessary dependencies.
- **Dependency Inversion Principle (DIP):** High-level modules do not depend on low-level modules; both depend on abstractions. This facilitates changes in implementation details without affecting the upper layers of the system.

## Testing:

Benchmark tests were conducted using `JMH`. Results include average time per operation for different data sizes:

- **Query Scalability:** Data available in the `query_benchmark_results.json` file.

- **Storage Scalability:** Data available in the `store_scalability_benchmark_results.json` file.

## Optional Tasks

Below are the additional tasks completed as part of the project:

- **UI Interface:** A graphical user interface was developed to provide a more intuitive and user-friendly way to interact with the system.

- **Nginx Report:** A detailed report was created explaining the usage and configuration of Nginx in the project, highlighting its integration, functionality, and key aspects for system improvement.

## Participants:

* **Alonso León, María** - [MariaAlonsoLeon](https://github.com/MariaAlonsoLeon)
* **Gil Bernal, Víctor** - [BeepBoopVictor](https://github.com/BeepBoopVictor)
* **Casimiro Torres, Kimberly** - [Kimberlycasimiro](https://github.com/Kimberlycasimiro)
