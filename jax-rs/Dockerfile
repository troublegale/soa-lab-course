FROM gradle:8-jdk17 AS build
WORKDIR /home/gradle/project

COPY . .

RUN gradle --no-daemon clean war

RUN bash -lc 'WAR="$(ls -1 build/libs/*.war | head -n 1)"; cp "$WAR" /tmp/orgmanager.war'



FROM quay.io/wildfly/wildfly

USER root

RUN /opt/jboss/wildfly/bin/add-user.sh -u wildfly -p wildfly! --silent

COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/
COPY --from=build /tmp/orgmanager.war /opt/jboss/wildfly/standalone/deployments/orgmanager.war
COPY jaxrs.keystore /opt/jboss/wildfly/standalone/configuration/
COPY truststore.jks /opt/jboss/wildfly/standalone/configuration/

RUN chown jboss:jboss \
    /opt/jboss/wildfly/standalone/configuration/jaxrs.keystore \
    /opt/jboss/wildfly/standalone/configuration/truststore.jks \
    /opt/jboss/wildfly/standalone/deployments/orgmanager.war

USER jboss

EXPOSE 8443

ENTRYPOINT exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 \
  -Djavax.net.ssl.trustStore=/opt/jboss/wildfly/standalone/configuration/truststore.jks \
  -Djavax.net.ssl.trustStorePassword=changeit
