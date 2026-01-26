FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY called-ejb/pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY called-ejb/src ./src
RUN mvn -B -DskipTests clean package

RUN cp target/called-ejb-1.0-SNAPSHOT.jar /tmp/called-ejb.jar

# Download PostgreSQL driver
RUN mvn dependency:copy -Dartifact=org.postgresql:postgresql:42.7.3 -DoutputDirectory=/tmp


FROM quay.io/wildfly/wildfly

USER root

RUN /opt/jboss/wildfly/bin/add-user.sh -u wildfly -p wildfly! --silent

# Install PostgreSQL JDBC driver
RUN mkdir -p /opt/jboss/wildfly/modules/org/postgresql/main
COPY --from=build /tmp/postgresql-42.7.3.jar /opt/jboss/wildfly/modules/org/postgresql/main/postgresql.jar
COPY called-ejb/module.xml /opt/jboss/wildfly/modules/org/postgresql/main/

COPY called-ejb/standalone.xml /opt/jboss/wildfly/standalone/configuration/
COPY --from=build /tmp/called-ejb.jar /opt/jboss/wildfly/standalone/deployments/called-ejb.jar
COPY called-ejb/certs/ejb.keystore /opt/jboss/wildfly/standalone/configuration/
COPY called-ejb/certs/truststore.jks /opt/jboss/wildfly/standalone/configuration/

RUN chown -R jboss:jboss /opt/jboss/wildfly/modules/org/postgresql && \
    chown jboss:jboss \
    /opt/jboss/wildfly/standalone/configuration/ejb.keystore \
    /opt/jboss/wildfly/standalone/configuration/truststore.jks \
    /opt/jboss/wildfly/standalone/deployments/called-ejb.jar

USER jboss

EXPOSE 8080 8443 9990

ENTRYPOINT exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 \
  -Djavax.net.ssl.trustStore=/opt/jboss/wildfly/standalone/configuration/truststore.jks \
  -Djavax.net.ssl.trustStorePassword=changeit
