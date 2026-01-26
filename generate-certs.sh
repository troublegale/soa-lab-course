#!/bin/bash

# Generate keystore for EJB server
keytool -genkeypair -alias ejb-server \
  -keyalg RSA -keysize 2048 \
  -validity 365 \
  -keystore called-ejb/certs/ejb.keystore \
  -storepass changeit \
  -keypass changeit \
  -dname "CN=called-ejb, OU=SOA, O=ITMO, L=SPB, S=SPB, C=RU" \
  -ext "SAN=DNS:called-ejb,DNS:localhost"

# Generate keystore for Web server
keytool -genkeypair -alias web-server \
  -keyalg RSA -keysize 2048 \
  -validity 365 \
  -keystore called-web/certs/web.keystore \
  -storepass changeit \
  -keypass changeit \
  -dname "CN=called-web, OU=SOA, O=ITMO, L=SPB, S=SPB, C=RU" \
  -ext "SAN=DNS:called-web,DNS:localhost"

# Export EJB certificate
keytool -exportcert -alias ejb-server \
  -keystore called-ejb/certs/ejb.keystore \
  -storepass changeit \
  -file called-ejb/certs/ejb.cer

# Export Web certificate
keytool -exportcert -alias web-server \
  -keystore called-web/certs/web.keystore \
  -storepass changeit \
  -file called-web/certs/web.cer

# Create truststore for EJB (trust web)
keytool -importcert -alias web-server \
  -keystore called-ejb/certs/truststore.jks \
  -storepass changeit \
  -file called-web/certs/web.cer \
  -noprompt

# Create truststore for Web (trust ejb)
keytool -importcert -alias ejb-server \
  -keystore called-web/certs/truststore.jks \
  -storepass changeit \
  -file called-ejb/certs/ejb.cer \
  -noprompt

echo "Certificates generated successfully!"
