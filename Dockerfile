FROM openjdk:11-jre-slim

ARG JAR_FILE=target/*.jar

COPY ${JAR_FILE} fp.jar

COPY fpkeystore.pkcs12 .

ENTRYPOINT ["java","-jar","/fp.jar"]