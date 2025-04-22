FROM maven:3.9.9
WORKDIR /app/Backend
COPY mesi_backend_rpg/pom.xml ./
RUN mvn install -DskipTests
COPY mesi_backend_rpg/src ./src
EXPOSE 8080
