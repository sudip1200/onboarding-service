FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY mcp-server/pom.xml /workspace/pom.xml

# Pre-fetch dependencies for better layer caching
RUN mvn -q -DskipTests dependency:go-offline

COPY mcp-server/src /workspace/src

RUN mvn -q -DskipTests package


FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 9092

ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
