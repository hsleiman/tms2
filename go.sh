#!/bin/sh
gradle clean
gradle build --debug --stacktrace

read -p "Press enter to continue"

java -jar build/libs/tms2-0.0.1-SNAPSHOT.jar
