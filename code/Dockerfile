FROM insideo/jre8
EXPOSE 6881
ADD /target/oauth2-code-1.0.0.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-java-config-dir=/config -jar /server.jar"]