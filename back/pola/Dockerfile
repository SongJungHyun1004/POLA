FROM openjdk:21-jdk-slim

WORKDIR /app

# build.gradle 
COPY build.gradle settings.gradle gradlew ./ 
# gradle
COPY gradle ./gradle
# src
COPY src ./src

# /app/gradlew 를 실행
RUN chmod +x ./gradlew
RUN ./gradlew bootjar


RUN mv build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
