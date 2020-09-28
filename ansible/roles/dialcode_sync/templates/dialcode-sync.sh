#!/bin/bash
echo "CSV file name: $1"
echo "Channel passed: $2"
i=1
while IFS=, read identifier
do
	test $i -eq 1 && ((i=i+1)) && continue # can be removed if header is not there in dialcodes.csv
	curl -X POST \
  http://localhost:9001/v3/dialcode/sync?identifier=${identifier} \
  -H 'Content-Type: application/json' \
  -H 'X-Channel-Id: '$2 \
  -H 'cache-control: no-cache' \
  -d '{
    "request":{
        "sync":{
            "channel": "'$2'"
        }
    }
}'
echo
done < $1
