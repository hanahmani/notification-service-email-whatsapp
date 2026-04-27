# ─── Stage 1 : Build ────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copie le pom.xml en premier pour profiter du cache Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copie les sources et build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2 : Runtime ──────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Création d'un utilisateur non-root pour la sécurité
RUN addgroup -S robocare && adduser -S robocare -G robocare

# Copie du JAR buildé
COPY --from=builder /app/target/notification-service-1.0.0.jar app.jar

# Changer le propriétaire
RUN chown robocare:robocare app.jar

USER robocare

EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/notifications/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
