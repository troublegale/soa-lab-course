FROM quay.io/wildfly/wildfly

RUN /opt/jboss/wildfly/bin/add-user.sh -u wildfly -p wildfly! --silent

ADD target/*.war /opt/jboss/wildfly/standalone/deployments/

COPY soa-spring.keystore /opt/jboss/wildfly/standalone/configuration/
COPY standalone.xml /opt/jboss/wildfly/standalone/configuration/

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]
