# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/payroll-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE 8080

CMD ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]