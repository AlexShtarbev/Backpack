FROM openjdk:23-jdk
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

COPY --chmod=0755 start.sh .
RUN chmod +x start.sh

ENTRYPOINT ["/start.sh"]
