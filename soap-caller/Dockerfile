# Stage 1: Build WAR
FROM maven:3.9-eclipse-temurin-11 AS build
WORKDIR /app

# Cache dependencies
COPY soap-caller/pom.xml ./
RUN mvn -B dependency:go-offline

# Build application
COPY soap-caller/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Deploy to WildFly
FROM quay.io/wildfly/wildfly:26.1.3.Final-jdk11

# Copy WAR file
COPY --from=build /app/target/soap-caller.war /opt/jboss/wildfly/standalone/deployments/

# Expose ports
EXPOSE 8080 9990

# Start WildFly
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
