#!/bin/bash
set -e

echo "Setting up application..."

# Configurer les fichiers de configuration avec gestion d'erreur
if [ ! -z "${FIREBASE_SERVICE_ACCOUNT_BASE64:-}" ]; then
    # Tentative de décodage avec détection d'erreur
    if echo "$FIREBASE_SERVICE_ACCOUNT_BASE64" | base64 -d > /app/src/main/resources/firebase-service-account.json 2>/dev/null; then
        echo "Firebase configuration created successfully."
    else
        echo "WARNING: Failed to decode Firebase configuration. Creating empty file."
        echo "{}" > /app/src/main/resources/firebase-service-account.json
    fi
else
    echo "WARNING: No Firebase configuration provided. Creating empty file."
    echo "{}" > /app/src/main/resources/firebase-service-account.json
fi

# Créer application.properties avec des valeurs par défaut
cat > /app/src/main/resources/application.properties <<EOF
spring.datasource.url=jdbc:mysql://localhost:3306/mesi_rpg?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=${MYSQL_ROOT_PASSWORD:-root}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Configuration Firebase
firebase.web.api.key=${FIREBASE_API_KEY:-dummy_key}
email=${ADMIN_EMAIL:-admin@example.com}
mdp=${ADMIN_PASSWORD:-password}

# désactiver l'initialisation Firebase si nécessaire
spring.profiles.active=${SPRING_PROFILES_ACTIVE:-dev}
EOF

echo "Starting MySQL..."
service mysql start

# Vérifier si MySQL est démarré (avec 30 secondes de timeout)
echo "Waiting for MySQL..."
for i in {1..30}; do
    if mysqladmin ping -h localhost --silent; then
        echo "MySQL started in $i seconds"
        break
    fi
    
    if [ $i -eq 30 ]; then
        echo "WARNING: MySQL startup timeout, but continuing..."
    fi
    
    sleep 1
done

# Configurer MySQL même si le ping a échoué
echo "Configuring MySQL database..."
{
    mysql -u root <<EOF
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_ROOT_PASSWORD:-root}';
CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE:-mesi_rpg};
GRANT ALL ON *.* TO 'root'@'%' IDENTIFIED BY '${MYSQL_ROOT_PASSWORD:-root}';
FLUSH PRIVILEGES;
EOF
} || echo "Database setup failed but continuing..."

echo "Starting Spring Boot application..."
exec java -jar /app/app.jar