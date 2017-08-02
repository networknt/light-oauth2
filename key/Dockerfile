FROM openjdk:8-jre-alpine
EXPOSE 6886
ADD /target/oauth2-key-1.3.5.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-4j-config-dir=/config -Dlogback.configurationFile=/config/logback.xml -jar /server.jar"]