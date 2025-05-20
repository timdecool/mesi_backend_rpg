#!/bin/bash
set -eo pipefail

# Variables de configuration
MYSQL_DATABASE=${MYSQL_DATABASE:-mesi_rpg}
MYSQL_USER=${MYSQL_USER:-root}
MYSQL_PASSWORD=${MYSQL_PASSWORD:-root}
MYSQL_PORT=${MYSQL_PORT:-3306}
APP_DIR="/app"
RESOURCES_DIR="${APP_DIR}/src/main/resources"
MYSQL_SOCKET="/var/run/mysqld/mysqld.sock"
MAX_MYSQL_WAIT=30

# Gestion de l'arrêt propre
mysql_pid=""
cleanup() {
    echo "Arrêt des services..."
    if [ -n "$mysql_pid" ]; then
        echo "Arrêt de MySQL (PID: $mysql_pid)"
        kill -TERM "$mysql_pid" 2>/dev/null
        wait "$mysql_pid" 2>/dev/null
    fi
    echo "Nettoyage terminé"
    exit 0
}
trap cleanup SIGINT SIGTERM

# Configuration de Firebase
setup_firebase() {
    echo "Configuration de Firebase..."

    if [ -n "$FIREBASE_SERVICE_ACCOUNT_BASE64" ]; then
        echo "Décodage de la configuration Firebase depuis la variable d'environnement..."
        echo "$FIREBASE_SERVICE_ACCOUNT_BASE64" | base64 -d >"${RESOURCES_DIR}/firebase-service-account.json"
    fi

    # Vérification/création du fichier Firebase
    if [ ! -s "${RESOURCES_DIR}/firebase-service-account.json" ]; then
        echo "ATTENTION: Fichier de configuration Firebase manquant ou vide. Création d'un fichier par défaut."
        echo "{}" >"${RESOURCES_DIR}/firebase-service-account.json"
    else
        echo "Fichier de configuration Firebase trouvé."
    fi
}

# Configuration de l'application
setup_application() {
    echo "Configuration de l'application Spring Boot..."

    # Création du fichier application.properties avec les variables d'environnement
    cat >"${RESOURCES_DIR}/application.properties" <<EOF
spring.application.name=mesi_backend_rpg
server.port=8080
server.address=0.0.0.0
spring.jpa.hibernate.ddl-auto=update
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.datasource.url=jdbc:mysql://localhost:${MYSQL_PORT}/${MYSQL_DATABASE}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=${MYSQL_USER}
spring.datasource.password=${MYSQL_PASSWORD}
spring.jackson.time-zone=Europe/Paris
spring.jpa.show-sql=false
firebase.web.api.key=${FIREBASE_API_KEY:-default_key}
email=${ADMIN_EMAIL:-admin@example.com}
mdp=${ADMIN_PASSWORD:-password}
anthropic.model=claude-3-7-sonnet-20250219
spring.websocket.enabled=true
anthropic.api.key=${ANTHROPIC_API_KEY:-default_key}
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.servlet.multipart.enabled=true
spring.servlet.multipart.file-size-threshold=2MB
EOF
}

# Configuration et démarrage de MySQL
setup_mysql() {
    echo "Configuration de MySQL..."

    # Préparation des répertoires MySQL
    mkdir -p /var/run/mysqld
    chown -R mysql:mysql /var/run/mysqld /var/lib/mysql
    chmod 777 /var/run/mysqld

    # Configuration MySQL minimaliste
    cat >/tmp/my.cnf <<EOF
[mysqld]
user=mysql
datadir=/var/lib/mysql
socket=${MYSQL_SOCKET}
bind-address=0.0.0.0
port=${MYSQL_PORT}
default_authentication_plugin=mysql_native_password
skip-name-resolve
innodb_buffer_pool_size=64M
max_connections=50
performance_schema=OFF

[client]
socket=${MYSQL_SOCKET}
EOF

    # Démarrage de MySQL en arrière-plan
    echo "Démarrage de MySQL..."
    mysqld --defaults-file=/tmp/my.cnf &
    mysql_pid=$!

    # Attente du démarrage de MySQL
    echo "Attente du démarrage de MySQL..."
    for i in $(seq 1 $MAX_MYSQL_WAIT); do
        if mysqladmin ping -h localhost --silent 2>/dev/null; then
            echo "MySQL démarré avec succès."
            init_mysql_database
            return 0
        fi
        echo "Tentative $i/$MAX_MYSQL_WAIT..."
        sleep 1
    done

    echo "ERREUR: Délai d'attente dépassé pour le démarrage de MySQL. Tentative de continuer quand même."
    return 1
}

# Initialisation de la base de données
init_mysql_database() {
    echo "Initialisation de la base de données..."

    # Tentatives de connexion avec différents mots de passe
    if mysql -u root -p${MYSQL_PASSWORD} -e "SELECT 1" >/dev/null 2>&1; then
        echo "Connecté avec le mot de passe '${MYSQL_PASSWORD}'"
    elif mysql -u root -e "SELECT 1" >/dev/null 2>&1; then
        echo "Connecté sans mot de passe. Configuration d'un mot de passe..."
        mysql -u root -e "
            CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE};
            ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '${MYSQL_PASSWORD}';
            CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '${MYSQL_PASSWORD}';
            GRANT ALL ON *.* TO 'root'@'%';
            FLUSH PRIVILEGES;
        "
    else
        echo "ATTENTION: Échec de connexion à MySQL. L'application pourrait ne pas fonctionner correctement."
    fi

    # Création de la base de données
    mysql -u ${MYSQL_USER} -p${MYSQL_PASSWORD} -e "CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE};" || true
}

# Démarrage de l'application Spring Boot
start_application() {
    echo "Démarrage de l'application Spring Boot..."
    java \
        -XX:+UseContainerSupport \
        -XX:MaxRAMPercentage=75.0 \
        -Dspring.config.location=file:${RESOURCES_DIR}/application.properties \
        -Djava.security.egd=file:/dev/./urandom \
        -jar ${APP_DIR}/app.jar
}

# Fonction principale
main() {
    echo "Démarrage du serveur..."
    setup_firebase
    setup_application
    setup_mysql
    start_application
}

# Exécution du script
main