# Command to get student dashboard. This command gets the list of enrolled courses for a student \
and for each enrolled course, gets the following details: \
	- Current Lecture \
	- Progress, Grade, Completion Count, Target Count \
	- Top Students, Top Colleges and Top Grades in each course

package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.common.dto Response

# command to get list of all courses for the given user_id. This command returns Response object
set resp_get_courses [ums_getcourses $user_id]

set get_course_error [check_resp_error $resp_get_courses]
if {$get_course_error} {
	puts "Error response from ums_getcourses"
	return $resp_get_courses
} else {
	# uses the system command 'get_resp_value' to get the course id list from Response object
	set course_list [get_resp_value $resp_get_courses "course_list"]

	# For each course, iterate and populate the required data
	# Create a list of Response value object maps
	set response_list [java::new ArrayList]
	java::for {String course_id} $course_list {
		# get current lecture command
		set resp_get_current_lecture [ums_getcurrent_lecture $user_id $course_id]
		# get progress details command
		set resp_get_progress [ums_getprogress $user_id $course_id]
		# get leaderboard data command
		set resp_leaderboard [ums_getleaderboard $course_id]
	
		# Use merge_response system command to merge the response value objects of the three responses. \
		Result of this command is a map of all the value objects. If one of the responses is an error, this \
		command throws an exception. 
		set merged_response [merge_response $resp_get_current_lecture $resp_get_progress $resp_leaderboard]

		# add merged response to a Response list
		$response_list add $merged_response
	}
	# TODO: instead of executing the commands remotely, the above loop can be sent and executed remotely \
	on the UMS server itself using 'execScriptRemote' command

	# Create the Response object with response id 'student.dashboard' and add the list of course dashboard maps \
	($dashboard_list) as response parameter with 'data' as the key
	set dashboard_response [create_response "student.dashboard" "data" $response_list]
	return $dashboard_response
}




