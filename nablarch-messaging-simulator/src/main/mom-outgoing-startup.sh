#! /bin/sh -x

java -Drequests-to-send="mom-RequestsToSend-103.csv" -classpath lib/*:src/main/resources:nablarch-messaging-simulator.jar nablarch.fw.launcher.Main -diConfig outgoing-simulator-component-configuration.xml -requestPath xxx -userId xxx