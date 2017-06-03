FROM openjdk:8-jre-alpine
EXPOSE 6883
ADD /target/oauth2-service-1.3.1.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-4j-config-dir=/config -jar /server.jar"]