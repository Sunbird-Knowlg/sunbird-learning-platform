#!/bin/bash
#set -x

##Variables##
NOW=$(date +"%F-%T")
LOG_PATH=logs/userlogs/$4
CONTAINER_NAME=$1
ACCOUNT_NAME=$2
ACCOUNT_KEY=$3
application_id=$4
IP=$(hostname -i)

if [ -d $LOG_PATH ] ; then
 zip -r $IP-$NOW.zip $LOG_PATH
 az storage blob upload -c $CONTAINER_NAME -n $IP-$NOW.zip -f $IP-$NOW.zip --account-name $ACCOUNT_NAME --account-key $ACCOUNT_KEY
 status=$?
 if [ $status != 0 ] ; then
 echo "something went wrong: $status - push  failed"
 exit 1
 fi
 rm $IP-$NOW.zip
else
 echo "$LOG_PATH is not exist"
 exit 1
fi
