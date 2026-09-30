# 1. Elige una imagen base oficial con Java (JRE 17)
FROM eclipse-temurin:17-jre-alpine

# 2. Define el directorio de trabajo dentro del contenedor
WORKDIR /app

# 3. Copia el archivo JAR generado (reemplaza 'mi-aplicacion.jar' por el nombre real que se generó en tu carpeta target)
COPY target/Validation-0.0.1-SNAPSHOT.jar app.jar

# 4. Expone el puerto de Spring Boot
EXPOSE 8080

# 5. Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]