FROM ubuntu:latest AS build

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY ./target/lab1-1.jar /app

EXPOSE 8080


CMD ["java", "-jar", "lab1-1.jar"]