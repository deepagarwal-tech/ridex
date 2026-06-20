# Stage 1: build the application with Maven (Maven itself comes from this
# image, so no local Maven installation or wrapper jar is needed).
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Pre-fetch dependencies in their own layer so repeated builds are faster
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B -DskipTests clean package

# Stage 2: run the built jar on a lean JRE-only image
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
