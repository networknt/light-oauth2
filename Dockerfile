FROM insideo/jre8
EXPOSE 8080
ADD /target/oauth2-0.1.2.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-java-config-dir=/config -jar /server.jar"]