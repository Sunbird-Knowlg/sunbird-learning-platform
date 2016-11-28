#!/bin/sh
if [ `ps -ef | grep logstash-word-events.conf | grep -v grep | wc -l` -ge 1 ]
then 
  kill -9 `pgrep -f logstash-word-events.conf`
fi