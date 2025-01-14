FROM openjdk:23-jdk-slim

# Copy the jar file into the container. $JAR_FILE is a build argument and can be set during the build process.
ARG JAR_FILE=build/libs/booklet-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Run the application.
ENTRYPOINT ["java", "-jar", "app.jar"]
