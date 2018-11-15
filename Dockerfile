FROM gradle:jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/project
WORKDIR /home/gradle/project
RUN gradle bootJar

FROM openjdk:10-jre-slim
EXPOSE 8080
COPY --from=builder /home/gradle/project/build/libs/link-endpoint-0.0.1-SNAPSHOT.jar /app/
WORKDIR /app
CMD java -jar link-endpoint-0.0.1-SNAPSHOT.jar
