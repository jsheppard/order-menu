# Build context must be the parent directory:
#   fly deploy --build-context ../
#   docker build -f order-menu/Dockerfile ..

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# --- order-models ---
COPY order-models/pom.xml ./order-models/pom.xml
COPY order-models/src     ./order-models/src
RUN cd order-models && mvn install -DskipTests -q

# --- order-client ---
COPY order-client/pom.xml ./order-client/pom.xml
COPY order-client/src     ./order-client/src
RUN cd order-client && mvn install -DskipTests -q

# --- order-menu ---
COPY order-menu/pom.xml ./order-menu/pom.xml
COPY order-menu/src     ./order-menu/src
RUN cd order-menu && mvn package -Pnative -DskipTests -q

# ---- Runtime ----
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /build/order-menu/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
