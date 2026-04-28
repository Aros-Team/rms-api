FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Aros Team"
LABEL description="RMS API - Restaurant Management System"

# Copiar JAR
COPY build/libs/*.jar app.jar

EXPOSE 8080


ENV JAVA_OPTS="-XX:+UseZGC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
