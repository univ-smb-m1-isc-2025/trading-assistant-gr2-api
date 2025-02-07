# Utiliser une image contenant OpenJDK 17 et Maven
FROM maven:3.8.6-amazoncorretto-17 AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier pom.xml et télécharger les dépendances
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copier tout le projet et compiler
COPY . .
RUN mvn clean package -DskipTests

# Étape finale avec une image légère de Java 17
FROM amazoncorretto:17-alpine
WORKDIR /app

# Copier l'application compilée
COPY --from=build /app/target/*.jar app.jar

# Exposer le port 8080
EXPOSE 8080

# Commande pour exécuter l'application
CMD ["java", "-jar", "app.jar"]
