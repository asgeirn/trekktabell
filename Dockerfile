FROM docker.io/library/maven:3-openjdk-17-slim AS builder
WORKDIR /src
ADD . /src
RUN mvn package

FROM cgr.dev/chainguard/jre:latest
EXPOSE 8080
WORKDIR /app
COPY --from=builder /src/target/libs/ /app/libs/
COPY --from=builder /src/target/*.jar /app/
CMD [ "-jar", "/app/trekkrutine-2025-0-SNAPSHOT.jar" ]
