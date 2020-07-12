FROM java:8-jdk-alpine
COPY ./target/voter-queue-1.0-SNAPSHOT-jar-with-dependencies.jar /usr/app/
WORKDIR /usr/app
EXPOSE 7000
ENTRYPOINT ["java", "-jar", "voter-queue-1.0-SNAPSHOT-jar-with-dependencies.jar"]


## RUN THE FOLLOWING COMMANDS
# mvn clean compile assembly:single
# docker build -t voter-queue .
# docker run --rm -it voter-queue:latest
