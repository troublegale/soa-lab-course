FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:resolve dependency:resolve-plugins -B

# Build application
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
