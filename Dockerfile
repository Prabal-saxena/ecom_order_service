# Use a lightweight OpenJDK image as base
FROM openjdk:17-jdk-slim

# Copy the JAR file into the container
ARG JAR_FILE=target/order_service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Expose the port your app runs on (e.g., 8080)
EXPOSE 8080

# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "/app.jar"]
