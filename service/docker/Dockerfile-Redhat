FROM registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift
ADD target/oauth2-service.jar server.jar
CMD ["/bin/sh","-c","java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -Dlight-4j-config-dir=/config -Dlogback.configurationFile=/config/logback.xml -jar server.jar"]
