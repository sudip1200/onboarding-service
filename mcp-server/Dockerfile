FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml /workspace/pom.xml
COPY mcp-server/pom.xml /workspace/mcp-server/pom.xml

# Pre-fetch dependencies for better layer caching
RUN mvn -q -pl mcp-server -am -DskipTests dependency:go-offline

COPY mcp-server/src /workspace/mcp-server/src

RUN mvn -q -pl mcp-server -am -DskipTests package


FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/mcp-server/target/*.jar /app/app.jar

EXPOSE 9092

ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]

