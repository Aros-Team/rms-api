FROM eclipse-temurin:21-jre

LABEL maintainer="Aros Team"
LABEL version="0.1.0"
LABEL description="RMS API - Restaurant Management System"

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseZGC -XX:+AlwaysPreTouch"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
