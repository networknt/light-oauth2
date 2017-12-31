FROM openjdk:8-jre-alpine
EXPOSE 6882
ADD /target/oauth2-token.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-4j-config-dir=/config -Dlogback.configurationFile=/config/logback.xml -jar /server.jar"]
