FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
RUN mvn -B -DskipTests dependency:go-offline

COPY . .
RUN mvn -B -DskipTests clean package

RUN bash -lc 'WAR="$(ls -1 target/*.war | head -n 1)"; cp "$WAR" /tmp/soa.war'



FROM quay.io/wildfly/wildfly

USER root

RUN /opt/jboss/wildfly/bin/add-user.sh -u wildfly -p wildfly! --silent

COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/
COPY --from=build /tmp/soa.war /opt/jboss/wildfly/standalone/deployments/soa.war
COPY spring.keystore /opt/jboss/wildfly/standalone/configuration/
COPY truststore.jks /opt/jboss/wildfly/standalone/configuration/

RUN chown jboss:jboss \
    /opt/jboss/wildfly/standalone/configuration/spring.keystore \
    /opt/jboss/wildfly/standalone/configuration/truststore.jks \
    /opt/jboss/wildfly/standalone/deployments/soa.war

USER jboss

EXPOSE 8443

ENTRYPOINT exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 \
  -Djavax.net.ssl.trustStore=/opt/jboss/wildfly/standalone/configuration/truststore.jks \
  -Djavax.net.ssl.trustStorePassword=changeit
