
#!/bin/sh
#****************************************************
#AUTHOR:MANOJ V V
#DESCRIPTION:Run orchestration scripts
#DATE:24-08-16
#************************************************

cd platform-tools/scripts/orchestrator/api
for file in *
do
	if [ -d $file ]
	then
		cd $file
		for tcl_file in *.tcl
		do
			tcl_str=`echo $tcl_file | rev | cut -c 5- | rev`
			json_file="register-"$tcl_str".json"
			node ../../registerScript.js $tcl_file json/$json_file $1
		done
	fi
done
cd
cd platform-tools/scripts/orchestrator/qe
for tcl_file in *.tcl
 do
 	tcl_str=`echo $tcl_file | rev | cut -c 5- | rev`
 	json_file="register-"$tcl_str".json"
	node ../registerScript.js $tcl_file json/$json_file $1
done