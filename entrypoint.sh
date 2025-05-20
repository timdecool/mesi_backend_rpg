#!/bin/bash
set -e

echo "Setting up application with Firebase..."

if [ -n "$FIREBASE_SERVICE_ACCOUNT_BASE64" ]; then
    echo "Decoding Firebase configuration from secret..."
    echo "$FIREBASE_SERVICE_ACCOUNT_BASE64" | base64 -d > /app/src/main/resources/firebase-service-account.json
fi

# Vérification du fichier Firebase
if [ -f /app/src/main/resources/firebase-service-account.json ]; then
    echo "Firebase configuration file exists."
    if [ ! -s /app/src/main/resources/firebase-service-account.json ]; then
        echo "WARNING: Firebase file is empty! Creating a default file."
        echo "{}" > /app/src/main/resources/firebase-service-account.json
    fi
else
    echo "WARNING: Firebase configuration file does not exist. Creating empty file."
    echo "{}" > /app/src/main/resources/firebase-service-account.json
fi

# Créer application.properties avec un mot de passe à essayer
# (il semble qu'il y ait peut-être déjà un mot de passe root existant)
cat > /app/src/main/resources/application.properties <<EOF
spring.application.name=mesi_backend_rpg
server.port=8080
server.address=0.0.0.0
spring.jpa.hibernate.ddl-auto=update
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.datasource.url=jdbc:mysql://localhost:3306/mesi_rpg?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.jackson.time-zone=Europe/Paris
spring.jpa.show-sql=false
firebase.web.api.key=${FIREBASE_API_KEY:-default_key}
email=${ADMIN_EMAIL:-admin@example.com}
mdp=${ADMIN_PASSWORD:-password}
anthropic.model=claude-3-7-sonnet-20250219
spring.websocket.enabled=true
anthropic.api.key=${ANTHROPIC_API_KEY:-default_key}
EOF

# Configuration MySQL
echo "Preparing MySQL..."
mkdir -p /var/run/mysqld
chown -R mysql:mysql /var/run/mysqld
chmod 777 /var/run/mysqld
chown -R mysql:mysql /var/lib/mysql

# Démarrer MySQL avec un fichier de configuration minimal
echo "Starting MySQL with custom configuration..."
cat > /tmp/my.cnf <<EOF
[mysqld]
user=mysql
datadir=/var/lib/mysql
socket=/var/run/mysqld/mysqld.sock
bind-address=0.0.0.0
port=3306
default_authentication_plugin=mysql_native_password
skip-name-resolve

[client]
socket=/var/run/mysqld/mysqld.sock
EOF

# Démarrer MySQL
mysqld --defaults-file=/tmp/my.cnf &
MYSQL_PID=$!

# Attendre que MySQL démarre
echo "Waiting for MySQL to start..."
MAX_TRIES=30
for i in $(seq 1 $MAX_TRIES); do
    echo "Try $i of $MAX_TRIES..."
    if mysqladmin ping -h localhost --silent 2>/dev/null; then
        echo "MySQL started successfully"
        
        # Essayer de se connecter avec différents mots de passe
        echo "Testing MySQL connection with common passwords..."
        if mysql -u root -proot -e "SELECT 1" >/dev/null 2>&1; then
            echo "Connected with password 'root'"
            # Configurer la base de données
            mysql -u root -proot -e "CREATE DATABASE IF NOT EXISTS mesi_rpg;"
            break
        elif mysql -u root -ppassword -e "SELECT 1" >/dev/null 2>&1; then
            echo "Connected with password 'password'"
            # Configurer la base de données
            mysql -u root -ppassword -e "CREATE DATABASE IF NOT EXISTS mesi_rpg;"
            # Mettre à jour application.properties
            sed -i 's/spring.datasource.password=root/spring.datasource.password=password/g' /app/src/main/resources/application.properties
            break
        elif mysql -u root -e "SELECT 1" >/dev/null 2>&1; then
            echo "Connected with empty password"
            # Configurer la base de données et définir un mot de passe
            mysql -u root -e "
                CREATE DATABASE IF NOT EXISTS mesi_rpg;
                ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root';
                CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY 'root';
                GRANT ALL ON *.* TO 'root'@'%';
                FLUSH PRIVILEGES;
            "
            break
        else
            echo "Common passwords didn't work, will need manual intervention later"
            break
        fi
    fi
    
    sleep 1
    if [ $i -eq $MAX_TRIES ]; then
        echo "MySQL startup timed out. Continuing anyway..."
    fi
done

echo "Starting Spring Boot application..."
exec java -Dspring.config.location=file:/app/src/main/resources/application.properties -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar