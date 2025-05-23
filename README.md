# Guide de lancement du projet Java

Ce document explique les étapes nécessaires pour configurer et lancer le projet Java.

## Prérequis

- JDK installé (version recommandée par l'équipe)
- Maven installé
- Un serveur avec un port MySQL ouvert
- Accès aux fichiers de configuration du projet

## Étapes de configuration

### 1. Configuration des fichiers essentiels

Créez un dossier `resources` dans le répertoire `src/main` s'il n'existe pas déjà :

```text
src/
└── main/
    └── resources/
```

Dans ce dossier, créez deux fichiers de configuration essentiels :

- `application.properties`
- `firebase-service-account.json`

> **Important** : Vous devez demander le contenu de ces fichiers aux développeurs du projet. Ces fichiers contiennent
> des informations sensibles comme les identifiants de base de données et les clés d'API Firebase.

### 2. Configuration de la base de données MySQL

*Ignorer cette étape dans le cadre d'un lancement avec Docker.*

Assurez-vous que :

- Votre serveur MySQL est en cours d'exécution
- Le port MySQL est ouvert et accessible
- Les identifiants de connexion MySQL correspondent à ceux spécifiés dans le fichier `application.properties`

### 3. Synchronisation de Maven

Avant de lancer le projet, synchronisez Maven pour télécharger toutes les dépendances nécessaires :

```bash
# À la racine du projet
mvn clean install
```

Ou utilisez l'option de synchronisation Maven depuis votre IDE (Eclipse, IntelliJ IDEA, etc.).

### 4. Lancement du projet

Une fois les étapes précédentes complétées, vous pouvez lancer le projet

## Lancement avec Docker

Le fichier *compose.yaml* est configuré pour un lancement local dans un cadre de développement. Pour lancer le projet, il faut que l'engine de Docker tourne sur la machine, puis entrer les commandes suivantes :

```bash
docker compose build #Construire l'image
docker compose up #Lancer l'image
```

Pour reconstruire l'image si besoin, d'abord exécuter

```bash
docker compose down -v
```

Puis de nouveau les commandes ci-dessus.

## Résolution des problèmes courants

- **Erreur de connexion à la base de données** : Vérifiez que le serveur MySQL est en cours d'exécution et que les
  informations de connexion dans `application.properties` sont correctes.
- **Dépendances manquantes** : Exécutez à nouveau la synchronisation Maven.
- **Erreur d'authentification Firebase** : Vérifiez que le fichier `firebase-service-account.json` est correctement
  formaté et contient les bonnes informations.

## Contact

Si vous rencontrez des problèmes lors de la configuration, contactez l'équipe de développement pour obtenir de l'aide.
