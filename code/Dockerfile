FROM insideo/jre8
EXPOSE 6881
ADD /target/oauth2-code-1.2.4.jar server.jar
CMD ["/bin/sh","-c","java -Dlight-java-config-dir=/config -jar /server.jar"]