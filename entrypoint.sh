#!/bin/bash
set -e

echo "Setting up application with Firebase..."

# Créer le répertoire de ressources
mkdir -p /app/src/main/resources

# Décoder la configuration Firebase
echo "$FIREBASE_SERVICE_ACCOUNT_BASE64" | base64 -d > /app/src/main/resources/firebase-service-account.json
echo "Firebase configuration decoded."

# Créer application.properties
cat > /app/src/main/resources/application.properties <<EOF
spring.application.name=mesi_backend_rpg
server.port=8080
spring.jpa.hibernate.ddl-auto=update
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.datasource.url=jdbc:mysql://localhost:3306/mesi_rpg?createDatabaseIfNotExist=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=
spring.jackson.time-zone=Europe/Paris
spring.jpa.show-sql=true
debug=false
firebase.web.api.key=${FIREBASE_API_KEY:-default_key}
email=${ADMIN_EMAIL:-admin@example.com}
mdp=${ADMIN_PASSWORD:-password}
anthropic.model=claude-3-7-sonnet-20250219
spring.websocket.enabled=true
anthropic.api.key=${ANTHROPIC_API_KEY:-default_key}
EOF

echo "Starting MySQL..."
service mysql start

# Attendre que MySQL démarre
echo "Waiting for MySQL to start..."
for i in {1..60}; do
    if mysqladmin ping -h localhost --silent; then
        echo "MySQL started successfully after $i seconds"
        break
    fi
    sleep 1
    
    if [ $i -eq 60 ]; then
        echo "MySQL not responding, but continuing..."
    fi
done

# Configuration de la base de données
echo "Configuring MySQL database..."
mysql -u root <<EOF || echo "MySQL configuration failed, but continuing..."
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root';
CREATE DATABASE IF NOT EXISTS mesi_rpg;
GRANT ALL ON *.* TO 'root'@'%' IDENTIFIED BY 'root';
FLUSH PRIVILEGES;
EOF

echo "Starting Spring Boot application..."
exec java -jar /app/app.jar