#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
BTH_JAVAC="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
BTH_JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
mkdir -p build/core-test
mapfile -t BTH_SOURCES < <(find src/main/java/com/betterthanherobrine/{horror,events,config} -name '*.java' -print)
"$BTH_JAVAC" --release 25 -d build/core-test "${BTH_SOURCES[@]}" src/test/java/com/betterthanherobrine/DirectorTest.java
"$BTH_JAVA" -ea -cp build/core-test com.betterthanherobrine.DirectorTest
