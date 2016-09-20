# IBM Cloud Architecture - Microservices Reference Application for Netflix OSS

## Netflix OSS on Bluemix - Zuul Edge Proxy

#### Description
  This project contains a packaged [Zuul Proxy](https://github.com/Netflix/zuul) edge proxy server for use in a [Netflix OSS](http://netflix.github.io/)-based microservices architecture.  This enables individual microservices to dynamically proxy & route communications between external and internal components.  The repository builds the Zuul component into a runnable JAR that can either be used directly in Cloud Foundry or built into a Docker image (with the [Dockerfile](https://github.com/ibm-cloud-architecture/microservices-netflix-zuul/blob/master/docker/Dockerfile) provided).

  This repository, and its parent reference application, are built to enable deployment and learning of building and operating microservices-based applications on the IBM Cloud, but due to the OSS-based nature of the components involved, this reference application can be deployed to any cloud or on-premises environment as desired.

#### Parent Reference Application
  **This project is part of the [IBM Cloud Architecture - Microservices Reference Application for Netflix OSS](https://github.com/ibm-cloud-architecture/microservices-netflix*) suite.**

  For full reference application overviews and deployment guidance, please refer to the root repository above.  The overall project consists of multiple sub projects:

  - Standard Netflix OSS-based microservice architecture components, like Eureka and Zuul
  - Sample Spring Boot applications which provide a REST API and communication between each other.
  - Deployment pipeline and automation guidance

The **Microservices Reference Application for Netflix OSS** is maintained by the IBM Cloud Lab Services and [Cloud Solution Engineering](https://github.com/ibm-cloud-architecture) teams.

#### Application Architecture
1.  **IBM Cloud Architecture - Microservices Reference Application for Netflix OSS**  
    The Microservices Reference Application for Netflix OSS leverages Zuul as its main edge proxy mechanism, controlling all inbound traffic into the application.  You can see where Eureka is used, highlighted in the diagram below.  
    ![Microservices RefApp Architecture](static/imgs/netflix-oss-wfd-arch-zuul.png?raw=true)
2.  **IBM Cloud Architecture - Cloud Native Microservices Reference Application for OmniChannel**  
    The Zuul Proxy component is also leveraged in the [OmniChannel Application](https://github.com/ibm-cloud-architecture/refarch-cloudnative) as its main proxy interface between external and internal microservices.  You can see where Zuul is used, highlighted in the diagram below.  
    ![OmniChannel Application Architecture](static/imgs/omnichannel-arch-zuul.png?raw=true)

#### APIs in this application:
There are no explicit APIs exposed by Zuul.  It is meant to be an API Gateway to the other APIs in your application.

#### Pre-requisites:
- Install Java JDK 1.8 and ensure it is available in your PATH
- _(Optional)_ A local Docker instance (either native or docker-machine based) running on localhost to host container(s). [Click for instructions](https://docs.docker.com/machine/get-started/).
- _(Optional)_ Apache Maven is used for an alternate build system.  [Click for instructions](https://maven.apache.org/install.html).

#### Build the Application Component
1.  Run one of the provided build scripts to compile the Java code, package it into a runnable JAR, and build the Docker image.  Run either  
        `./build-mvn.sh`  
  or  
        `./build-gradle.sh`  
  to run the Maven or Gradle builds, respectively.  Both build packages produce the same output, however both build files are provided for convenience of the user.

#### Run the Application Component Locally
1.  You will need a local [Eureka](https://github.com/ibm-cloud-architecture/microservices-netflix-eureka) application instance running to connect to, from which Zuul will proxy requests to additional service instances.

2.  You can now run either the JAR file or the Docker image locally.  

    1.1.  To run the JAR file locally, you can simply pass parameters to the Java command in the command prompt:  
        `java -jar docker/app.jar`  
    1.2.  To run the Docker file locally, you can pass the same paramters to start the local Docker image:  
        `docker run -p 8080:8080 microservices-refapp-zuul:latest`  

3.  Verify there is a Zuul Proxy service visible in your Eureka Dashboard at `http://localhost:8761/`.

#### Run the Application Component on Bluemix
1.  You will need a [Eureka](https://github.com/ibm-cloud-architecture/microservices-netflix-eureka) application instance running to connect to, from which Zuul will proxy requests to additional service instances.  The instance of Eureka must be accessible to the Zuul containers from public Bluemix.  Make note of this fully-qualified URL. _(eg http://microservices-refapp-eureka-cloudarch.mybluemix.net/eureka/)_

2.  Edit the Bluemix Response File to select your desired external public route, application domain, and other operational details.  The default values in the `.bluemixrc` are acceptable to deploy in to the US-South Bluemix region.

3.  To deploy Zuul as a container group onto the Bluemix Container Service, execute the following script and pass in the previously-noted Eureka location:  
        `./deploy-container-group.sh http://microservices-refapp-eureka-cloudarch.mybluemix.net/eureka/`  

    This script will create a clustered group of homogeneous containers, with additional management capabilities provided by Bluemix.  The parameter passed into the script is the location of the Eureka service discovery container group, so that the Zuul Proxy container group can register with it upon startup.

4.  The script will complete rather quickly, but the creation of the necessary Container Group and clustered containers may take a few moments. To check on the status of your Zuul Container Group, you can run the following command:  
        `cf ic group ls | grep zuul_cluster`  

    Once you see a value for *Status* of `CREATE_COMPLETED`, your Zuul Proxy instance will now be publicly accessible through the URL configured in the Bluemix response file.  

#### Validate the Application Component Deployment
1.  Validate that the Eureka user interface appears after a few seconds of the application being started.  
2.  Verify that there is a registered `zuul-proxy` microservice registered with Eureka, visible in the Eureka Dashboard.  
3.  You can now run additional services that register with Eureka and contact them through the Zuul Proxy.  For instance, a service registered as `weather-service` in Eureka is accessible through the following Zuul Proxy URL:  
  3.1. Locally:  http://localhost:8080/weather-service/ (with _localhost:8080_ being the default location of the running Zuul Proxy instance)  
  3.2. Bluemix:  http://microservices-refapp-zuul-cloudarch.mybluemix.net/weather-service (with _microservices-refapp-zuul-cloudarch_ being your configured `PROXY_HOSTNAME` in the .bluemixrc file)  
