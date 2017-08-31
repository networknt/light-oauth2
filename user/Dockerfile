FROM openjdk:8-jre-alpine
EXPOSE 6885
ADD /target/oauth2-user-1.4.2.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-4j-config-dir=/config -Dlogback.configurationFile=/config/logback.xml -jar /server.jar"]