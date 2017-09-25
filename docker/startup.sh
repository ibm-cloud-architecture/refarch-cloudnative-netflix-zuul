#!/bin/bash
set -e

# Set basic java options
export JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"

# find the java heap size as 50% of container memory using sysfs, or 512m whichever is less
max_heap=`echo "512 * 1024 * 1024" | bc`
if [ -r "/sys/fs/cgroup/memory/memory.limit_in_bytes" ]; then
    mem_limit=`cat /sys/fs/cgroup/memory/memory.limit_in_bytes`
    if [ ${mem_limit} -lt ${max_heap} ]; then
        max_heap=${mem_limit}
    fi
fi
max_heap=`echo "(${max_heap} / 1024 / 1024) / 2" | bc`
export JAVA_OPTS="${JAVA_OPTS} -Xmx${max_heap}m"

# Checks for eureka "variable" set by Kubernetes secret
if [ -z ${eureka_service+x} ] || [ -z ${eureka_port+x} || [ -z ${eureka_prefer_ip_address+x}]; then
    echo "Not all \"eureka\" variables are set!";
else
    echo "Setting up Eureka"

    # Construct eureka url
    eureka_url="http://${eureka_service}:${eureka_port}/eureka/"
    JAVA_OPTS="${JAVA_OPTS} -Deureka.client.serviceUrl.defaultZone=${eureka_url}"
    JAVA_OPTS="${JAVA_OPTS} -Deureka.instance.hostname=http://${eureka_service}"
    JAVA_OPTS="${JAVA_OPTS} -Deureka.instance.preferIpAddress=${eureka_prefer_ip_address}"
fi

# Load agent support if required
source ./agents/newrelic.sh

echo "Starting with Java Options ${JAVA_OPTS}"

# Start the application
exec java ${JAVA_OPTS} -jar /app.jar