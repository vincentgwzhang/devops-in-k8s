# ------------------------
# Stage 1: Build JAR
# ------------------------
FROM eclipse-temurin:25-jdk AS builder

WORKDIR /app

# Copy Maven project files
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline

# Copy source and build
COPY src ./src
RUN ./mvnw -DskipTests package


# ------------------------
# Stage 2: Runtime Image
# ------------------------
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy built JAR
COPY --from=builder /app/target/*.jar app.jar

# Set JVM flags optimized for container
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Expose the port
EXPOSE 8080

# Startup command
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
