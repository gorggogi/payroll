# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (cached if pom.xml unchanged)
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN ./mvnw package -DskipTests -B

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/payroll-0.0.1-SNAPSHOT.jar app.jar

# Render sets PORT; Spring Boot defaults to 8080
ENV PORT=8080
EXPOSE 8080

# Run on the port Render provides
CMD ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]