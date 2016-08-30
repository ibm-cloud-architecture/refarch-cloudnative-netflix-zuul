#!/bin/bash

SCRIPTDIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
source $SCRIPTDIR/.bluemixrc

#################################################################################
# Validate image exists in Bluemix registry
#################################################################################
echo "Looking up Bluemix registry images"
BLUEMIX_IMAGES=$(cf ic images --format "{{.Repository}}:{{.Tag}}")

REQUIRED_IMAGES=(
    ${PROXY_IMAGE}
)

for image in ${REQUIRED_IMAGES[@]}; do
    echo "$BLUEMIX_IMAGES" | grep "$image" > /dev/null
    if [ $? -ne 0 ]; then
        #echo "Pulling ${DOCKERHUB_NAMESPACE}/$image from Dockerhub"
        echo "Proxy Image Not Found.  Attempting to push from Local Docker"
        DOCKER_IMAGES=$(docker images --format "{{.Repository}}:{{.Tag}}")
        echo "$DOCKER_IMAGES" | grep "$image" > /dev/null
        if [ $? -ne 0 ]; then
          echo "Local Proxy Image Not Found.  Exiting..."
          exit 1
        fi
        docker tag ${PROXY_IMAGE} ${BLUEMIX_REGISTRY_HOST}/${BLUEMIX_REGISTRY_NAMESPACE}/${PROXY_IMAGE}
        docker push ${BLUEMIX_REGISTRY_HOST}/${BLUEMIX_REGISTRY_NAMESPACE}/${PROXY_IMAGE}
    fi
done


#################################################################################
# Start Eureka container group
#################################################################################
echo "Starting Zuul Proxy container group"
echo "...with Eureka Cluster available at ${1}"
cf ic group create --name zuul_cluster \
  --publish 8080 --memory 256 --auto \
  --min 1 --max 3 --desired 1 \
  --hostname ${PROXY_HOSTNAME} \
  --domain ${PROXY_DOMAIN} \
  --env eureka.client.serviceUrl.defaultZone=${1} \
  --env eureka.instance.hostname=${REGISTRY_HOSTNAME}.${ROUTES_DOMAIN} \
  ${BLUEMIX_REGISTRY_HOST}/${BLUEMIX_REGISTRY_NAMESPACE}/${PROXY_IMAGE}
