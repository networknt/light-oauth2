FROM insideo/jre8
EXPOSE 6882
ADD /target/oauth2-token-1.2.4.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-java-config-dir=/config -jar /server.jar"]