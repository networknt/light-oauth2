FROM networknt/oauth2-build:latest
FROM openjdk:8-jre-alpine
RUN mkdir /app
WORKDIR /app
COPY --from=0 /app/token/target/oauth2-token.jar server.jar
VOLUME /config
ENTRYPOINT java -Dlight-4j-config-dir=/config -Dlogback.configurationFile=/config/logback.xml -cp server.jar com.networknt.server.Server
