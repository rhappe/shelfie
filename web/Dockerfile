FROM eclipse-temurin:21 AS builder

# Install a minimal Android SDK so AGP can configure :app during the Gradle build.
# The wasmJs compilation itself does not use Android SDK — only the configuration phase needs it.
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
COPY gradlew gradlew.bat ./
COPY gradle ./gradle/
RUN chmod +x gradlew

COPY . .
RUN ./gradlew :app:wasmJsBrowserDistribution --no-daemon

FROM nginx:alpine
COPY --from=builder /build/app/build/dist/wasmJs/productionExecutable /usr/share/nginx/html
EXPOSE 80
