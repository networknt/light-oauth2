FROM insideo/jre8
EXPOSE 6884
ADD /target/oauth2-client-1.0.0.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-java-config-dir=/config -jar /server.jar"]