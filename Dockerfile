FROM maven:3.9.9 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -Dmaven.compiler.forceJavacCompilerUse=true

FROM eclipse-temurin:21-jre-jammy

# Installation de MySQL
RUN apt-get update && apt-get install -y \
    mysql-server \
    procps \
    netcat \
    && rm -rf /var/lib/apt/lists/* && \
    mkdir -p /var/run/mysqld

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
RUN mkdir -p /app/src/main/resources
COPY firebase-service-account.json /app/src/main/resources/
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Volume pour les données MySQL
VOLUME /var/lib/mysql

EXPOSE 8080 3306
ENTRYPOINT ["/app/entrypoint.sh"]