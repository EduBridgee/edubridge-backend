# Etapa de construcción (prepara y compila tu código)
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa de ejecución (crea el servidor final súper liviano)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto 8081 que configuraste en tu properties
EXPOSE 8081

# Comando para encender Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
