#!/bin/bash

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

JDK21_HOME="$PROJECT_DIR/../../jdk-21"

if [ -d "$JDK21_HOME" ]; then
    export JAVA_HOME="$(cd "$JDK21_HOME" && pwd)"
    export PATH="$JAVA_HOME/bin:$PATH"
else
    JAVA_VER=$(java -version 2>&1 | head -1)
    if echo "$JAVA_VER" | grep -q '"21\.' ; then
        JAVA_BIN="$(which java)"
        JAVA_REAL="$(readlink -f "$JAVA_BIN")"
        export JAVA_HOME="$(dirname "$(dirname "$JAVA_REAL")")"
    else
        exit 1
    fi
fi

java -version 2>&1
mvn -version 2>&1 | head -3

mvn clean package -DskipTests

APP_NAME="project-jfx-client"
APP_VERSION="1.0"
MAIN_MODULE="project.jfx.client"
MAIN_CLASS="com.project.app.ProjectClientApplication"
DEST_DIR="$PROJECT_DIR/target/jpackage"
MODULES_DIR="$PROJECT_DIR/target/modules"
CLASSES_DIR="$PROJECT_DIR/target/classes"
APP_JAR="$PROJECT_DIR/target/${APP_NAME}-${APP_VERSION}.jar"

rm -rf "$DEST_DIR"

cp "$APP_JAR" "$MODULES_DIR/"

JMODS_DIR="$JAVA_HOME/jmods"

if [ -d "$JMODS_DIR" ]; then
    jpackage \
        --type app-image \
        --name "$APP_NAME" \
        --module-path "$JMODS_DIR:$MODULES_DIR:$CLASSES_DIR" \
        --module "$MAIN_MODULE/$MAIN_CLASS" \
        --dest "$DEST_DIR" \
        --java-options "-Dfile.encoding=UTF-8"
else
    APP_INPUT_DIR="$PROJECT_DIR/target/app-input"
    rm -rf "$APP_INPUT_DIR"
    mkdir -p "$APP_INPUT_DIR"
    cp "$APP_JAR" "$APP_INPUT_DIR/"
    cp "$MODULES_DIR"/*.jar "$APP_INPUT_DIR/" 2>/dev/null || true

    jpackage \
        --type app-image \
        --name "$APP_NAME" \
        --runtime-image "$JAVA_HOME" \
        --input "$APP_INPUT_DIR" \
        --main-jar "${APP_NAME}-${APP_VERSION}.jar" \
        --main-class "$MAIN_CLASS" \
        --dest "$DEST_DIR" \
        --java-options "-Dfile.encoding=UTF-8" \
        --java-options "-p \$APPDIR" \
        --java-options "--add-modules=$MAIN_MODULE"
fi

if [ -d "$PROJECT_DIR/db" ]; then
    cp -r "$PROJECT_DIR/db" "$DEST_DIR/$APP_NAME/db"
else
    mkdir -p "$DEST_DIR/$APP_NAME/db"
fi
