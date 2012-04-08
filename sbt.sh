#!/bin/sh
java -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m -Xmx712M -Xss2M -jar "sbt-launch.jar" "$@"
