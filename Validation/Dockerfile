# Etapa 1: Compilación con Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: Imagen final ligera para correr la app
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/Validation-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]