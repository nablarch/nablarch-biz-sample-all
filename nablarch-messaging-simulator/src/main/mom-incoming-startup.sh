#! /bin/sh -x

java -Drequest-id="RM11AC0203" -classpath lib/*:src/main/resources:nablarch-messaging-simulator.jar nablarch.fw.launcher.Main -diConfig incoming-mom-simulator-component-configuration.xml -requestPath xxx -userId xxx