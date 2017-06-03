package require java
java::import -package java.util HashMap Map
java::import -package java.util ArrayList LinkedList List
java::import -package org.apache.commons.lang3 StringEscapeUtils

set externalProps [java::new ArrayList]
$externalProps add "stageIcons"
set env_String [java::cast String $server_env]
set env_text [$env_String toString]
set env_text "${env_text}/v3/content/${content_id}"
set is_env_null [java::isnull $server_env]
if { $is_env_null == 1} {
	set result_map [java::new HashMap]
	$result_map put "code" "SERVER_ERROR"
	$result_map put "message" "Something went wrong while processing"
	$result_map put "responseCode" [java::new Integer 400]
	set response_list [create_error_response $result_map]
	return $response_list
}
set resp_object [java::new HashMap]
set stage [java::new LinkedList]
set externalPropId $content_id
set bodyResponse [getContentProperties $externalPropId $externalProps]
set check_error [check_response_error $bodyResponse]
if {!$check_error} {
	set extValues [get_resp_value $bodyResponse "values"]
	set is_extValues_null [java::isnull $extValues]
	set result_map [java::new HashMap]
	if {$is_extValues_null == 0} {
		set extValuesMap [java::cast Map $extValues]
		$resp_object putAll $extValuesMap
		set data [$resp_object get "stageIcons"]
		set is_data_null [java::isnull $data]
                if {$is_data_null == 1} {
			$result_map put "code" "ERR_CONTENT_INVALID_REQUEST"
			$result_map put "message" "Content does not have stageIcons"
			$result_map put "responseCode" [java::new Integer 400]
			set response_list [create_error_response $result_map]
			return $response_list
		} 
		set data [java::cast String $data]
		set stage_string [java::call StringEscapeUtils unescapeJson $data]
		set stage_map [convert_jsonstring_to_map $stage_string]
		$stage addAll [$stage_map keySet]
		set stage_list [java::new LinkedList]
		java::for {String elem} $stage {
   			$stage_list add "${env_text}/stage/${elem}?format=base64"
		}
	} else {
		$result_map put "code" "ERR_CONTENT_INVALID_REQUEST"
		$result_map put "message" "Content doesnot exist"
		$result_map put "responseCode" [java::new Integer 400]
		set response_list [create_error_response $result_map]
		return $response_list
	}
	$result_map put "stages" $stage_list
	set response_list [create_response $result_map]
	return $response_list
} else {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_CONTENT_INVALID_CONTENT"
	$result_map put "message" "Content"
	$result_map put "responseCode" [java::new Integer 400]
	set response_list [create_error_response $result_map]
	return $response_list
}
