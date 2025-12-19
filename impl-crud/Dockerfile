FROM amazoncorretto:25

# Set a working directory
WORKDIR /app

# Argument to accept the JAR file name
ARG JAR_FILE=target/*.jar

# Copy the packaged jar file into the container
COPY ${JAR_FILE} application.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "application.jar"]
