FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml ./
COPY frontend ./frontend
COPY src ./src
COPY db ./db

RUN mvn clean package -DskipTests

FROM tomcat:11.0-jdk21-temurin

ENV PORT=8080

RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/dokoTsubu.war /usr/local/tomcat/webapps/dokoTsubu.war
COPY docker/start-tomcat.sh /usr/local/bin/start-tomcat.sh
RUN chmod +x /usr/local/bin/start-tomcat.sh

EXPOSE 8080
CMD ["/usr/local/bin/start-tomcat.sh"]
