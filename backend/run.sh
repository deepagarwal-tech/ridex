#!/usr/bin/env bash
#
# run.sh
#
# Runs the Ridex Spring Boot app WITHOUT requiring a system-wide Maven
# installation. On first run, it downloads a local copy of Apache Maven
# into ./.maven (no admin rights, no PATH changes) and uses that.
#
# Requirements: Java 17+ must already be installed (check with `java -version`).
#
# Usage:
#   ./run.sh
#

set -e

MAVEN_VERSION="3.9.6"
MAVEN_DIR="$(pwd)/.maven"
MAVEN_HOME="$MAVEN_DIR/apache-maven-${MAVEN_VERSION}"
MAVEN_BIN="$MAVEN_HOME/bin/mvn"

if [ ! -x "$MAVEN_BIN" ]; then
    echo "Maven not found locally — downloading Apache Maven ${MAVEN_VERSION} (one-time setup)..."
    mkdir -p "$MAVEN_DIR"

    DOWNLOAD_URL="https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
    ARCHIVE_PATH="$MAVEN_DIR/maven.tar.gz"

    if command -v curl >/dev/null 2>&1; then
        curl -fsSL -o "$ARCHIVE_PATH" "$DOWNLOAD_URL"
    elif command -v wget >/dev/null 2>&1; then
        wget -q -O "$ARCHIVE_PATH" "$DOWNLOAD_URL"
    else
        echo "ERROR: Neither curl nor wget is available. Please install one and re-run."
        exit 1
    fi

    tar -xzf "$ARCHIVE_PATH" -C "$MAVEN_DIR"
    rm "$ARCHIVE_PATH"
    chmod +x "$MAVEN_BIN"

    echo "Maven downloaded to $MAVEN_HOME"
fi

echo "Starting Ridex (Spring Boot)..."
"$MAVEN_BIN" spring-boot:run
