# ---- STAGE 1: Build the app ----
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app

# Copy everything to the container
COPY . .

# Package the application (skip tests for faster build; remove -DskipTests if needed)
RUN mvn clean package -DskipTests

# ---- STAGE 2: Run the app ----
FROM eclipse-temurin:17-jdk
WORKDIR /app

# Copy the built jar from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Create data folder for H2 to persist
RUN mkdir -p /app/data

# Expose port (default is 8080, but it will use PORT env if set)
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
