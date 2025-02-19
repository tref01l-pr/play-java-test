FROM sbtscala/scala-sbt:eclipse-temurin-jammy-17.0.9_9_1.9.7_2.13.12 AS build

WORKDIR /app
COPY . /app

RUN sbt clean stage

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app
COPY --from=build /app/target/universal/stage/lib ./lib
COPY --from=build /app/target/universal/stage/bin ./bin
COPY --from=build /app/conf ./conf
COPY .env .env
COPY docker/docker-entrypoint.sh .

RUN chmod +x docker-entrypoint.sh

EXPOSE 9000

ENTRYPOINT ["./docker-entrypoint.sh"]