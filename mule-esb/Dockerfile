# Mule ESB Dockerfile
# Multi-stage build: Maven builds the Mule application, then deploy to Mule runtime

# Stage 1: Build the Mule application with Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml and download dependencies first (for Docker cache)
COPY mule-esb/pom.xml .
RUN mvn dependency:go-offline -B 2>/dev/null || true

# Copy source files
COPY mule-esb/src ./src
COPY mule-esb/mule-artifact.json .

# Build the Mule application
RUN mvn clean package -DskipTests -B

# Stage 2: Deploy to Mule runtime
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /opt/mule

# Install required tools
RUN apt-get update && apt-get install -y \
    unzip \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy and extract Mule distribution
COPY mule-esb/mule-ee-distribution-standalone-4.10.2.zip /tmp/
RUN unzip /tmp/mule-ee-distribution-standalone-4.10.2.zip -d /opt/ && \
    mv /opt/mule-enterprise-standalone-4.10.2 /opt/mule-standalone && \
    rm /tmp/mule-ee-distribution-standalone-4.10.2.zip

# Set environment variables
ENV MULE_HOME=/opt/mule-standalone
ENV PATH="${MULE_HOME}/bin:${PATH}"

# Copy custom log4j2 configuration for console logging
COPY mule-esb/conf/log4j2.xml ${MULE_HOME}/conf/log4j2.xml

# Copy built Mule application JAR from build stage
COPY --from=build /app/target/*.jar ${MULE_HOME}/apps/

# Expose HTTP port
EXPOSE 8082

# Default environment variables (can be overridden by docker-compose)
ENV HTTP_PORT=8082
ENV CALLED_WEB_HOST=called-web
ENV CALLED_WEB_PORT=8443

# Start Mule runtime in wrapperless mode (bypass Tanuki wrapper)
# Use -M-D to pass JVM system properties
CMD ["sh", "-c", "${MULE_HOME}/bin/mule wrapperless -M-Dhttp.port=${HTTP_PORT} -M-Dcalled.web.host=${CALLED_WEB_HOST} -M-Dcalled.web.port=${CALLED_WEB_PORT}"]