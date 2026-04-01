FROM eclipse-temurin:21 AS builder

# Install Android SDK (needed for AGP configuration phase only)
ENV ANDROID_HOME=/opt/android-sdk
RUN apt-get update -qq && apt-get install -qq -y wget unzip libatomic1 && rm -rf /var/lib/apt/lists/*
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip \
        -O /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin
RUN yes | sdkmanager --licenses && sdkmanager "platforms;android-35" "build-tools;35.0.0"

WORKDIR /build

# Cache Gradle wrapper
COPY gradlew gradlew.bat ./
COPY gradle ./gradle/
RUN chmod +x gradlew

# Cache dependencies — these layers only rebuild when build files change
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY server/build.gradle.kts server/build.gradle.kts
COPY shared/build.gradle.kts shared/build.gradle.kts
COPY app/build.gradle.kts app/build.gradle.kts
RUN ./gradlew :app:dependencies --no-daemon || true

# Copy source and build
COPY shared/ shared/
COPY app/ app/
RUN ./gradlew :app:wasmJsBrowserDistribution --no-daemon

FROM nginx:alpine
COPY --from=builder /build/app/build/dist/wasmJs/productionExecutable /usr/share/nginx/html
EXPOSE 80
