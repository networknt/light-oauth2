FROM jimschubert/8-jdk-alpine-mvn:latest
RUN mkdir /app
WORKDIR /app
COPY . .
RUN mvn package -Dmaven.test.skip=true \
    && rm -rf ${MAVEN_HOME}/.m2/repository
