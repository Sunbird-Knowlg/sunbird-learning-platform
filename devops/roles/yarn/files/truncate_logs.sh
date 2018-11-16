#!/bin/bash

# Truncate hadoop/yarn userlogs and keep the last 100 lines

HADOOP_LOGS_HOME=/usr/local/hadoop/logs/userlogs

for d in $HADOOP_LOGS_HOME/*/*/ ; do (cd $d && tail -n 100 stdout > stdout.tmp && cat stdout.tmp > stdout && rm stdout.tmp); done

LOGSTASH_LOGS=/var/log/logstash
tail -n 100 $LOGSTASH_LOGS/logstash.stdout > $LOGSTASH_LOGS/logstash.stdout.tmp
cat $LOGSTASH_LOGS/logstash.stdout.tmp > $LOGSTASH_LOGS/logstash.stdout
rm $LOGSTASH_LOGS/logstash.stdout.tmp

HADOOP_TMP_USERLOGS=/usr/local/hadoop/logs/userlogs

for g in $HADOOP_TMP_USERLOGS/*/*/ do
	cd $g
	tail -n 100 stdout > stdout.tmp 
	cat stdout.tmp > stdout
	rm stdout.tmp
done
