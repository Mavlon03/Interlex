# ---------- build ----------
# mvnw in this repo is empty, so the official Maven image builds the jar.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# dependencies first, so they are cached until pom.xml changes
COPY pom.xml ./
RUN mvn -q -B dependency:go-offline -DskipTests

COPY src src
RUN mvn -q -B package -DskipTests && cp target/*.jar app.jar

# ---------- run ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app --home /app app \
    && apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* \
    && chown -R app:app /app

COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar
USER app

# application-prod.properties binds to 127.0.0.1 (bare-metal setup); inside a container it must listen on all
# interfaces — the host still publishes the port only on 127.0.0.1 (see deploy/docker-compose.yml).
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_ADDRESS=0.0.0.0 \
    SERVER_PORT=8080 \
    TZ=Asia/Tashkent \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD curl -fs -o /dev/null http://localhost:8080/ || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
