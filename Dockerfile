FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml ./
COPY frontend ./frontend
COPY src ./src
COPY db ./db

RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
ENV PORT=8080

COPY --from=build /app/target/dokoTsubu.war app.war

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.war"]
