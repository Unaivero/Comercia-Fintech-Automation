# Multi-stage build for optimal image size
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build

# Set working directory
WORKDIR /app

# Copy POM and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean compile test-compile -B

# Runtime stage with Selenium capabilities
FROM selenoid/vnc:chrome_126.0

# Switch to root for installations
USER root

# Install Java 17, Maven, and other tools
RUN apt-get update && \
    apt-get install -y \
        openjdk-17-jdk \
        maven \
        wget \
        curl \
        jq \
        unzip \
        && apt-get clean \
        && rm -rf /var/lib/apt/lists/*

# Install Allure
RUN wget -q https://github.com/allure-framework/allure2/releases/download/2.25.0/allure-2.25.0.tgz && \
    tar -xzf allure-2.25.0.tgz -C /opt/ && \
    ln -s /opt/allure-2.25.0/bin/allure /usr/bin/allure && \
    rm allure-2.25.0.tgz

# Set JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="$JAVA_HOME/bin:$PATH"

# Create application directory
WORKDIR /test-framework

# Copy built application from build stage
COPY --from=build /app .
COPY --from=build /root/.m2/repository /root/.m2/repository

# Create directories for reports and logs
RUN mkdir -p target/allure-results target/cucumber-reports logs

# Install Chrome extensions for testing
RUN wget -q -O /tmp/chrome-extension.crx \
    "https://clients2.google.com/service/update2/crx?response=redirect&prodversion=91.0&x=id%3Dpadekgcemlokbadohgkifijomclgjgif%26installsource%3Dondemand%26uc"

# Health check for the container
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:4444/wd/hub/status || exit 1

# Create entrypoint script
RUN cat > /entrypoint.sh << 'EOF'
#!/bin/bash
set -e

echo "🚀 Starting Comercia Fintech Test Framework..."
echo "Environment: ${ENVIRONMENT:-docker}"
echo "Browser: ${BROWSER:-chrome}"
echo "Parallel Execution: ${PARALLEL_EXECUTION:-false}"

# Start VNC server if needed
if [ "${VNC_ENABLED:-false}" = "true" ]; then
    echo "🖥️ Starting VNC server..."
    vncserver :1 -geometry 1920x1080 -depth 24
fi

# Validate environment
echo "🔍 Validating environment..."
java -version
mvn -version
allure --version

# Clean previous results
echo "🧹 Cleaning previous results..."
rm -rf target/allure-results/* target/cucumber-reports/*

# Execute tests
echo "🧪 Executing tests..."
if [ "${PARALLEL_EXECUTION:-false}" = "true" ]; then
    echo "Running tests in parallel mode..."
    mvn clean test \
        -Dbrowser=${BROWSER:-chrome} \
        -Denvironment=${ENVIRONMENT:-docker} \
        -Dheadless.execution=${HEADLESS:-true} \
        -Dparallel.execution=true \
        -Dthread.count=${THREAD_COUNT:-3} \
        -Dmaven.test.failure.ignore=true
else
    echo "Running tests in sequential mode..."
    mvn clean test \
        -Dbrowser=${BROWSER:-chrome} \
        -Denvironment=${ENVIRONMENT:-docker} \
        -Dheadless.execution=${HEADLESS:-true} \
        -Dmaven.test.failure.ignore=true
fi

# Generate reports
echo "📊 Generating reports..."
mvn allure:report

# Keep container running if in interactive mode
if [ "${INTERACTIVE:-false}" = "true" ]; then
    echo "🎯 Container ready for interactive use"
    tail -f /dev/null
fi

echo "✅ Test execution completed!"
EOF

RUN chmod +x /entrypoint.sh

# Expose ports
EXPOSE 4444 5900 8080

# Set default environment variables
ENV ENVIRONMENT=docker
ENV BROWSER=chrome
ENV HEADLESS=true
ENV PARALLEL_EXECUTION=false
ENV THREAD_COUNT=3
ENV VNC_ENABLED=false
ENV INTERACTIVE=false

# Default command
ENTRYPOINT ["/entrypoint.sh"]
