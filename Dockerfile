# Étape de build: utilisation d'une image plus légère
FROM maven:3.9.9 AS builder

WORKDIR /app

# Copie et mise en cache des dépendances Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copie des sources et build
COPY src ./src
RUN mvn package -DskipTests -Dmaven.compiler.forceJavacCompilerUse=true

# Étape de l'image finale: utilisation d'une image plus légère
FROM eclipse-temurin:21-jre-jammy

# Installation de MySQL avec optimisation
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    mysql-server \
    procps \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /var/run/mysqld \
    && mkdir -p /app/src/main/resources \
    # Suppression des fichiers inutiles de MySQL pour réduire la taille
    && rm -rf /var/lib/mysql/mysql-bin.* \
    && rm -rf /var/lib/mysql/ib_logfile* \
    && rm -rf /var/lib/mysql/ibdata1 \
    && rm -rf /usr/share/mysql/charsets \
    && rm -rf /usr/share/mysql/czech \
    && rm -rf /usr/share/mysql/danish \
    && rm -rf /usr/share/mysql/dutch \
    && rm -rf /usr/share/mysql/english \
    && rm -rf /usr/share/mysql/estonian \
    && rm -rf /usr/share/mysql/french \
    && rm -rf /usr/share/mysql/german \
    && rm -rf /usr/share/mysql/greek \
    && rm -rf /usr/share/mysql/hungarian \
    && rm -rf /usr/share/mysql/italian \
    && rm -rf /usr/share/mysql/japanese \
    && rm -rf /usr/share/mysql/korean \
    && rm -rf /usr/share/mysql/norwegian \
    && rm -rf /usr/share/mysql/norwegian-ny \
    && rm -rf /usr/share/mysql/polish \
    && rm -rf /usr/share/mysql/portuguese \
    && rm -rf /usr/share/mysql/romanian \
    && rm -rf /usr/share/mysql/russian \
    && rm -rf /usr/share/mysql/serbian \
    && rm -rf /usr/share/mysql/slovak \
    && rm -rf /usr/share/mysql/spanish \
    && rm -rf /usr/share/mysql/swedish \
    && rm -rf /usr/share/mysql/ukrainian

# Configuration de l'application
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY entrypoint.sh /app/entrypoint.sh.orig
RUN cat /app/entrypoint.sh.orig | tr -d '\r' > /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh && \
    rm /app/entrypoint.sh.orig

# Volume pour les données MySQL
VOLUME /var/lib/mysql

# Ports exposés
EXPOSE 8080 3306

# Point d'entrée
ENTRYPOINT ["/app/entrypoint.sh"]