#!/usr/bin/env bash

function execute-dependencies() {
	echo "Configuring dependencies before build"
	mkdir -p ~/logs
	find ${TRAVIS_BUILD_DIR}/ -type f -name "log4j2.xml" -print0 | xargs -0 sed -i -e 's/\/data\/logs/~\/logs/g'
	curl -O https://artifacts.elastic.co/downloads/elasticsearch/elasticsearch-6.3.2.deb && sudo dpkg -i --force-confnew elasticsearch-6.3.2.deb && sudo service elasticsearch restart
	mvn install:install-file -Dfile=${TRAVIS_BUILD_DIR}/orchestrator/module/interpreter-api/lib/jtcl-2.7.0.jar -DgroupId=tcl.lang -DartifactId=jtcl -Dversion=2.7.0 -Dpackaging=jar
	sleep 10
	echo "Configuration of dependencies completed"
}