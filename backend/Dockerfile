FROM eclipse-temurin:21-jdk as builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test
FROM eclipse-temurin:21-jre
COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]