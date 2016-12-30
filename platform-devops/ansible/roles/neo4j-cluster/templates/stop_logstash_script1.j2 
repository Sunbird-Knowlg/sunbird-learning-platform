#!/bin/sh
if [ `ps -ef | grep logstash-graph-events.conf | grep -v grep | wc -l` -ge 1 ]
then 
  kill -9 `pgrep -f logstash-graph-events.conf`
fi

