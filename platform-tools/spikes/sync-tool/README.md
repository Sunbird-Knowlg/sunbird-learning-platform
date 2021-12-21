# Learning platform 

Repository for Learning Platform

## Sync-tool local setup

Prerequisites:

    Java 8
    elasticsearch:6.8.18
    Neo4j
    Redis
    Cassandra
    
### Running sync-tool :
1. Go to the path: ```/sunbird-learning-platform``` and run the below maven command to build the application.
```shell
mvn clean install -DskipTests
```
2. Run the below java command to run the sync-tool jar locally and sync up the object types to the search index.
```shell
java -Dconfig.file=./platform-tools/spikes/sync-tool/src/main/resources/application.conf -jar ./platform-tools/spikes/sync-tool/target/sync-tool-0.0.1-SNAPSHOT.jar sync --graph domain
```
