# IBM Cloud Architecture - Microservices Reference Application

## Netflix OSS on Bluemix - Zuul Edge Proxy

### Description
TBD

### Root Reference Application
https://github.com/ibm-cloud-architecture/microservices-netflix

### Application Architecture
TBD

### Run this Application Component

#### Build the Application Component
1.  Run one of the provided build scripts to compile the Java code, package it into a runnable JAR, and build the Docker image.  Run either  
        ./build-mvn.sh  
  or  
        ./build-gradle.sh  
  to run the Maven or Gradle builds, respectively.  Both build packages produce the same output, however both build files are provided for convenience of the user.

#### Run it Locally
1.  You will need a local [Eureka](https://github.com/ibm-cloud-architecture/microservices-netflix-eureka) application instance running to connect to, from which Zuul will proxy requests to additional service instances.

2.  You can now run either the JAR file or the Docker image locally.  

    1.1.  To run the JAR file locally, you can simply pass parameters to the Java command in the command prompt:  
        java -jar docker/app.jar  
    1.2.  To run the Docker file locally, you can pass the same paramters to start the local Docker image:  
        docker run -p 8080:8080 microservices-refapp-zuul:latest  

3.  Verify there is a Zuul Proxy service visible in your Eureka Dashboard:
        http://localhost:8761/

4.  You can run additional services that register with Eureka and contact them through the Zuul Proxy.  For instance, a service registered as `weather-service` in Eureka is accessible through the following Zuul Proxy URL (with _localhost:8080_ being the location of the running Zuul Proxy instance):
        http://localhost:8080/weather-service/

#### Run it on Bluemix
1.  You will need a local [Eureka](https://github.com/ibm-cloud-architecture/microservices-netflix-eureka) application instance running to connect to, from which Zuul will proxy requests to additional service instances.  The instance of Eureka must be accessible to the Zuul containers from public Bluemix.  Make note of this fully-qualified URL. _(eg http://microservices-refapp-eureka-cloudarch.mybluemix.net/eureka/)_

2.  Edit the Bluemix Response File to select your desired external public route, application domain, and other operational details.  The default values in the `.bluemixrc` are acceptable to deploy in to the US-South Bluemix region.

3.  To deploy Zuul as a container group onto the Bluemix Container Service, execute the following script and pass in the previously-noted Eureka location:
        ./deploy-container-group.sh http://microservices-refapp-eureka-cloudarch.mybluemix.net/eureka/
    This script will create a clustered group of homogeneous containers, with additional management capabilities provided by Bluemix.

4.  The script will complete rather quickly, but the creation of the necessary Container Group and clustered containers may take a few moments. To check on the status of your Zuul Container Group, you can run the following command:
        cf ic group ls | grep zuul_cluster
    Once you see a value for *Status* of `CREATE_COMPLETED`, your Zuul Proxy instance will now be publicly accessible through the URL configured in the Bluemix response file.  

    Verify there is a Zuul Proxy service visible in your Eureka Dashboard.  For example:
        http://microservices-refapp-eureka-cloudarch.mybluemix.net/


#### Validate Deployment
TBD
