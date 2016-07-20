#!/bin/sh
#****************************************************
#AUTHOR:MANOJ V V
#DESCRIPTION:Script for lp-definition-update.
#DATE:25-05-16
#****************************************************
cd /home/jenkins/workspace/LP_Definition_Update/docs/domain_model_v2/definitions                    #move inside th folder

#*****************************************************
#Find all files required & copy the content of the file to a variable .user variable in the curl command to definition-update
for file in *                        
do
   variable=`cat $file`
   `curl -i -X POST -H "Content-Type: application/json" -H "user-id: ansible_user" -d '$variable' http://54.254.225.115:8080/learning-service/taxonomy/domain/definition`
done


cd /home/jenkins/workspace/LP_Definition_Update/docs/language_model/definitions/iso                    #move inside th folder

#*****************************************************
#Find all files required & copy the content of the file to a variable .user variable in the curl command to definition-update
for file in *                        
do
   variable1=`cat $file`
   `curl -i -X POST -H "Content-Type: application/json" -H "user-id: ansible_user" -d '$variable1' http://54.254.225.115:8080/learning-service/taxonomy/domain/definition`
done



cd /home/jenkins/workspace/LP_Definition_Update/docs/language_model/definitions/language“                    #move inside th folder

#*****************************************************
#Find all files required & copy the content of the file to a variable .user variable in the curl command to definition-update
for file in *                        
do
	cd list
   for env in *                        
   do

   variable2=`cat $file`
   `curl -i -X POST -H "Content-Type: application/json" -H "user-id: ansible_user" -d '$variable2' http://54.254.225.115:8080/learning-service/taxonomy/domain/definition`
done
done