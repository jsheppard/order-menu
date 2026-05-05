FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /build

# --- Copy and install order-models and order-client from jars ---
COPY jars ./jars
RUN mvn install:install-file -Dfile=./jars/order-models-1.0.0-SNAPSHOT.jar -DgroupId=com.sbsolutions -DartifactId=order-models -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar -q && \
    mvn install:install-file -Dfile=./jars/order-client-1.0.0-SNAPSHOT.jar -DgroupId=com.sbsolutions -DartifactId=order-client -Dversion=1.0.0-SNAPSHOT -Dpackaging=jar -q

# --- order-menu ---
COPY pom.xml .
COPY src ./src
RUN mvn package -Pproduction -DskipTests -q

# ---- Runtime ----
FROM container-registry.oracle.com/graalvm/jdk:25

WORKDIR /app

# Create non-root user for container security
RUN groupadd -r app && useradd -r -g app app

COPY --from=build /build/target/*.jar app.jar
RUN chown app:app app.jar

USER app

EXPOSE 8082

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:InitialRAMPercentage=50.0", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:MinRAMPercentage=25.0", \
  "-XX:+UseG1GC", \
  "-XX:MaxGCPauseMillis=200", \
  "-XX:+ParallelRefProcEnabled", \
  "-XX:+AlwaysPreTouch", \
  "-XX:-OmitStackTraceInFastThrow", \
  "-Dfile.encoding=UTF-8", \
  "-Duser.timezone=America/Chicago", \
  "-jar", "app.jar"]
