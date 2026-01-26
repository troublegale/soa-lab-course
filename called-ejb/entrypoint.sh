#!/bin/bash

# Wait for Consul to be ready
wait_for_consul() {
    echo "Waiting for Consul..."
    while ! curl -sf http://consul:8500/v1/status/leader > /dev/null 2>&1; do
        sleep 2
    done
    echo "Consul is ready!"
}

# Register service in Consul
register_service() {
    local container_id=$(cat /proc/sys/kernel/hostname)
    local service_id="called-ejb-${container_id}"
    local service_name="called-ejb"
    local service_port=8443
    local service_host=$(cat /etc/hosts | grep $(cat /proc/sys/kernel/hostname) | awk '{print $1}')
    
    echo "Registering service ${service_name} with Consul..."
    
    curl -X PUT http://consul:8500/v1/agent/service/register \
        -H "Content-Type: application/json" \
        -d "{
            \"ID\": \"${service_id}\",
            \"Name\": \"${service_name}\",
            \"Address\": \"${service_host}\",
            \"Port\": ${service_port},
            \"Tags\": [\"ejb\", \"wildfly\", \"jakarta-ee\"],
            \"Check\": {
                \"HTTP\": \"http://${service_host}:9990/health\",
                \"Interval\": \"10s\",
                \"Timeout\": \"5s\",
                \"DeregisterCriticalServiceAfter\": \"60s\"
            }
        }"
    
    if [ $? -eq 0 ]; then
        echo "Service registered successfully!"
    else
        echo "Failed to register service"
    fi
}

# Deregister service on shutdown
deregister_service() {
    local container_id=$(cat /proc/sys/kernel/hostname)
    local service_id="called-ejb-${container_id}"
    echo "Deregistering service ${service_id} from Consul..."
    curl -X PUT "http://consul:8500/v1/agent/service/deregister/${service_id}"
}

# Trap signals for graceful shutdown
trap deregister_service SIGTERM SIGINT

# Wait for Consul and register
wait_for_consul
register_service

# Start WildFly in background, then wait
exec /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 \
  -Djavax.net.ssl.trustStore=/opt/jboss/wildfly/standalone/configuration/truststore.jks \
  -Djavax.net.ssl.trustStorePassword=changeit &

WILDFLY_PID=$!

# Wait for WildFly to exit
wait $WILDFLY_PID
