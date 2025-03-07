FROM maven:3-openjdk-17-slim AS builder
WORKDIR /src
ADD . /src
RUN mvn package

FROM gcr.io/distroless/java17-debian12
EXPOSE 8080
WORKDIR /app
COPY --from=builder /src/target/libs/ /app/libs/
COPY --from=builder /src/target/*.jar /app/
CMD [ "/app/trekkrutine-2025-0-SNAPSHOT.jar" ]
