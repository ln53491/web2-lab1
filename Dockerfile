FROM openjdk:17-alpine AS builder

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .
COPY src src
RUN chmod +x mvnw

COPY . /app
ENV FIREBASE_CONFIG=/ect/firebase-key.json


RUN ./mvnw clean package

FROM openjdk:17-alpine

COPY --from=builder target/*.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app.jar"]

