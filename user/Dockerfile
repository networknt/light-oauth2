FROM insideo/jre8
EXPOSE 6885
ADD /target/oauth2-user-1.0.0.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-java-config-dir=/config -jar /server.jar"]