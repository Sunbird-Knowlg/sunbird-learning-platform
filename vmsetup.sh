#!/bin/bash
sudo apt update
sudo apt install redis-server -y
curl -O https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.3.2.deb
sudo dpkg -i elasticsearch-6.3.2.deb
sudo service elasticsearch start
sudo service elasticsearch status
mvn install:install-file -Dfile=./orchestrator/module/interpreter-api/lib/jtcl-2.7.0.jar -DgroupId=tcl.lang -DartifactId=jtcl -Dversion=2.7.0 -Dpackaging=jar
mkdir -p ~/logs/search/
find ./ -type f -name "log4j2.xml" -print0 | xargs -0 sed -i -e 's/\/data\/logs/~\/logs/g'
find ./ -type f -name "logback.xml" -print0 | xargs -0 sed -i -e 's/\/data\/logs/logs/g'
find ./ -type f -name "application.conf" -print0 | xargs -0 sed -i -e 's/\/data\//~\//g'
find ./ -type f -name "*.java" -print0 | xargs -0 sed -i -e 's/\/data\//~\//g'
XML_REPORT_PATHS=`find /home/circleci/project  -iname jacoco.xml | awk 'BEGIN { RS = "" ; FS = "\n"; OFS = ","}{$1=$1; print $0}'`
mvn verify sonar:sonar -Dsonar.projectKey=project-sunbird_sunbird-learning-platform -Dsonar.organization=project-sunbird -Dsonar.host.url=https://sonarcloud.io -Dsonar.coverage.jacoco.xmlReportPaths=${XML_REPORT_PATHS} -Dsonar.scanner.force-deprecated-java-version-grace-period=true