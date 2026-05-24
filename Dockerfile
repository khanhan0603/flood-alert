FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY src src
RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar /app/app.jar
COPY render-start.sh /app/render-start.sh
RUN chmod +x /app/render-start.sh
ENV JAVA_OPTS="-Xms256m -Xmx768m -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["/app/render-start.sh"]
