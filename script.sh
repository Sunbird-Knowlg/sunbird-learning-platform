#!/bin/sh
#****************************************************
#AUTHOR:MANOJ V V
#DESCRIPTION:Script for lp-definition-update.
#DATE:25-05-16
#****************************************************
cd docs/domain_model_v2/definitions                    #move inside th folder

#*****************************************************
#Find all files required & copy the content of the file to a variable .user variable in the curl command to definition-update
for file in *                        
do
   variable=`cat $file`
   curl -i -X POST -H "Content-Type: application/json" -H "user-id: ansible_user" -d "$variable" http://54.254.225.115:8080/learning-service/taxonomy/domain/definition
done



#*****************************************************
cd docs/language_model/definitions/iso                    #move inside th folder

for file1 in *
do
   json_content1=`cat $file1`
   curl -i -X POST -H "Content-Type: application/json" -H "user-id: ansible_user" -d "$json_content1" http://54.254.225.115:8080/language-service/v1/language/language/importDefinition
done

