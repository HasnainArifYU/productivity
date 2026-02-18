FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# Copy maven executable and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make the mvnw script executable
RUN chmod +x mvnw

# Build all dependencies to cache them in a Docker layer
RUN ./mvnw dependency:go-offline -B

# Copy source files
COPY src src

# Build the application
RUN ./mvnw package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Use a smaller JRE runtime image for production
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp

# Copy the dependency application resources from build stage
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Set Spring profiles and define entry point
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-cp", "app:app/lib/*", "com.productivity.ProductivityAppApplication"]
