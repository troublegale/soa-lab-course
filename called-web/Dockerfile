# Stage 1: Build WAR
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copy called-ejb first (dependency for called-web)
COPY called-ejb/pom.xml /app/called-ejb/pom.xml
COPY called-ejb/src /app/called-ejb/src

# Build and install called-ejb locally
WORKDIR /app/called-ejb
RUN mvn clean install -DskipTests

# Copy called-web
WORKDIR /app
COPY called-web/pom.xml /app/called-web/pom.xml
COPY called-web/src /app/called-web/src

# Build called-web
WORKDIR /app/called-web
RUN mvn clean package -DskipTests

# Stage 2: Deploy to WildFly
FROM quay.io/wildfly/wildfly:latest

# Copy standalone configuration
COPY called-web/standalone.xml /opt/jboss/wildfly/standalone/configuration/standalone.xml

# Copy keystore and truststore
COPY called-web/certs/web.keystore /opt/jboss/wildfly/standalone/configuration/web.keystore
COPY called-web/certs/truststore.jks /opt/jboss/wildfly/standalone/configuration/truststore.jks

# Copy WAR file
COPY --from=build /app/called-web/target/soa.war /opt/jboss/wildfly/standalone/deployments/called-web.war

# Expose ports
EXPOSE 8080 8443 9990

# Start WildFly
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
