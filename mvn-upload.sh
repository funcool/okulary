#!/bin/sh
mvn deploy:deploy-file -Dfile=target/okulary.jar -DpomFile=pom.xml -DrepositoryId=clojars -Durl=https://clojars.org/repo/
