FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts build.gradle.kts ./
COPY baserow/ baserow/
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q -PskipLambda 2>/dev/null || true

COPY src/ src/
RUN ./gradlew build -x test --no-daemon -PskipLambda

FROM registry.access.redhat.com/ubi9/openjdk-21:1.23

ENV LANGUAGE='en_US:en'

COPY --from=build --chown=185 /app/build/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /app/build/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /app/build/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /app/build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT ["/opt/jboss/container/java/run/run-java.sh"]
