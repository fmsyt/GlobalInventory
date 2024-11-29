FROM maven:3.8.1-jdk-8-slim

WORKDIR /workspace

RUN mvn -B dependency:resolve dependency:resolve-plugins
RUN mvn -B clean package
