FROM openjdk:17-alpine

WORKDIR /app
COPY .env ./.env


COPY target/generator-0.0.1-SNAPSHOT.jar /app.jar
RUN chmod +x /app.jar
EXPOSE 8080
CMD ["java", "-jar", "/app.jar"]
