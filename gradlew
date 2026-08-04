#!/usr/bin/env sh

APP_HOME="`dirname \"$0\"`"
APP_HOME="`cd \"$APP_HOME\"; pwd`"
DEFAULT_JVM_OPTS=""

execute() {
    "${JAVA_HOME}/bin/java" $DEFAULT_JVM_OPTS -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
}

execute "$@"
