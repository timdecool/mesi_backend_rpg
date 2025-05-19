# Étape 1: Build de l'application Java avec Lombok correctement configuré
FROM maven:3.9.9 AS builder

# Installer les dépendances nécessaires
WORKDIR /app
COPY pom.xml .

# Configuration explicite de Lombok pour s'assurer qu'il fonctionne correctement
RUN echo "lombok.addLombokGeneratedAnnotation = true" > lombok.config
RUN echo "lombok.anyConstructor.addConstructorProperties=true" >> lombok.config

# Télécharger les dépendances en premier pour le caching
RUN mvn dependency:go-offline -B

# Copier le code source
COPY src ./src

# Compiler l'application avec Lombok activé
RUN mvn package -DskipTests -Dmaven.compiler.forceJavacCompilerUse=true

# Étape 2: Image finale avec Java et MySQL
FROM eclipse-temurin:21-jre-jammy

# Installation de MySQL et autres utilitaires
RUN apt-get update && apt-get install -y \
    mysql-server \
    procps \
    netcat \
    && rm -rf /var/lib/apt/lists/*

# Configurer MySQL
RUN mkdir -p /var/run/mysqld \
    && chown -R mysql:mysql /var/run/mysqld \
    && chmod 777 /var/run/mysqld

# Variable d'environnement pour MySQL
ENV MYSQL_DATABASE=mesi_rpg \
    MYSQL_ROOT_PASSWORD=root

# Copier l'application compilée
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Créer le répertoire resources
RUN mkdir -p /app/src/main/resources

# Copier le script d'entrée
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Volume pour les données MySQL
VOLUME /var/lib/mysql

# Exposer les ports
EXPOSE 8080 3306

# Démarrer l'application
ENTRYPOINT ["/app/entrypoint.sh"]