## Instances ##

### Dev ###

1. LP-Dev - 54.254.225.115

### QA ###

1. LP-QA - 54.179.153.127


### Prod ###
1. LP-Prod - 52.77.241.169

## Services running on all 3 above instances ##
tomcat
elasticsearch
mongod
redis
logstash 
  1)logstash-graph-events.conf
  2)language-graph-events.conf


**1) Tomcat**

```sh
# Check if the process is running
ps -ef | grep tomcat | grep -v grep

# Command to kill
sudo service tomcat stop

# Command to start the process
sudo service tomcat start
```

**2) Elasticsearch**

```sh 
# Check if the process is running
ps -ef | grep elasticsearch | grep -v grep

# Command to kill
sudo service elasticsearch stop

# Command to start the process
sudo service elasticsearch start
```

**3) Mongod Process**

```sh 
# Check if the process is running
ps -ef | grep mongod | grep -v grep

# Command to kill
sudo service mongod stop

# Command to start the process
sudo service mongod start
```

**4) Redis**

```sh 
# Get the process id
ps -ef | grep redis | grep -v grep

# Check if the process is running
ps pid

# Command to kill
kill pid

# Command to start the process
cd $REDIS_HOME/src/redis-server &
```



### Logstash ###

**1) logstash-graph-events.conf**

```sh 
# Check if the process is running
check=ps -ef | grep logstash | logstash-graph-events.conf | grep -v grep | wc -l


# Command to kill
check == 1
kill pid

# Command to start the process
check == 0
$LOGSTASH_HOME/bin/logstash -f $LOGSTASH_HOME/logstash-2.3.3/logstash-graph-events.conf -v &
```

**2) Language-graph-events.conf**

```sh 
# Check if the process is running
check=ps -ef | grep logstash | language-graph-events.conf | grep -v grep | wc -l

# Command to kill
check == 1
kill pid

# Command to start the process
$LOGSTASH_HOME/bin/logstash -f $LOGSTASH_HOME/logstash-2.3.3/language-graph-events.conf -v &
```
