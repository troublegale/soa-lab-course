FROM quay.io/wildfly/wildfly

USER root

COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/
COPY target/*.war /opt/jboss/wildfly/standalone/deployments/soa.war
COPY spring.keystore /opt/jboss/wildfly/standalone/configuration/
COPY truststore.jks /opt/jboss/wildfly/standalone/configuration/

RUN chown jboss:jboss \
    /opt/jboss/wildfly/standalone/configuration/spring.keystore \
    /opt/jboss/wildfly/standalone/configuration/truststore.jks \
    /opt/jboss/wildfly/standalone/deployments/soa.war

USER jboss

EXPOSE 8443

ENTRYPOINT exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 \
  -Djavax.net.ssl.trustStore=/opt/jboss/wildfly/standalone/configuration/truststore.jks \
  -Djavax.net.ssl.trustStorePassword=changeit