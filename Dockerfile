FROM maven:3.9.9 AS build
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN mvn dependency:go-offline -B
COPY src src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Ajouter des arguments pour optimiser la JVM
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseContainerSupport"

COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]