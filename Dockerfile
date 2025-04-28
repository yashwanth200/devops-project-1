# Start with a lightweight Java image
FROM openjdk:17-jdk-slim

# Add a label (optional)
LABEL maintainer="yashwanthvee30@gmail.com"

# Set working directory
WORKDIR /app

# Copy the JAR file to the container
COPY target/sb-ecom.jar app.jar

# Expose port (optional, if you want to access it)
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]
