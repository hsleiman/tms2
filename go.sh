#!/bin/sh
gradle clean
gradle build --debug --stacktrace

echo read -p "Press enter to continue"

cp ./build/libs/*.war /Users/hsleiman/CloudStation/Servers/apache-tomcat-8.0.30/webapps
