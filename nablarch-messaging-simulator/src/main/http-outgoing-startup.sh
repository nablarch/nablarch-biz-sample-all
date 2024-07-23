#! /bin/sh -x

java -Drequests-to-send="http-RequestsToSend-102.csv" -classpath lib/*:src/main/resources:nablarch-messaging-simulator.jar nablarch.fw.launcher.Main -diConfig outgoing-http-simulator-component-configuration.xml -requestPath xxx -userId xxx -sendCount 1