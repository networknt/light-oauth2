
FROM registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift
#EXPOSE  6889
ADD target/oauth2-provider server.jar
CMD ["/bin/sh","-c","java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -Dlight-4j-config-dir=/config -Dlogback.configurationFile=/config/logback.xml -jar server.jar"]
