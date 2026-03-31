FROM eclipse-temurin:21 AS builder
WORKDIR /build
COPY . .
RUN chmod +x gradlew

# Provide a stub ANDROID_HOME so AGP can locate an SDK root without crashing.
# AGP 8.x defers SDK resolution to task execution — building :server:buildFatJar
# never executes Android tasks, so the real SDK is never needed.
ENV ANDROID_HOME=/opt/android-sdk
RUN mkdir -p ${ANDROID_HOME}

RUN ./gradlew :server:buildFatJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/server/build/libs/server-all.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
