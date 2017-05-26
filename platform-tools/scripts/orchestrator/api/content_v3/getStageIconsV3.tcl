package require java
java::import -package java.util HashMap LinkedHashMap Map
java::import -package java.util ArrayList LinkedList List
java::import -package org.apache.commons.lang3 StringEscapeUtils

set externalProps [java::new ArrayList]
$externalProps add "stageIcons"
set resp_object [java::new HashMap]
set stage_map [java::new LinkedHashMap]
set stage_list [java::new LinkedList]
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
		} else {
			set data [java::cast String $data]
			set stage_string [java::call StringEscapeUtils unescapeJson $data]
			$stage_map putAll [convert_jsonstring_to_map $stage_string]
			set stage [$stage_map get $stage_id]
			set is_stage_null [java::isnull $stage]
			if {$is_stage_null == 1} {
				$result_map put "code" "ERR_CONTENT_INVALID_REQUEST"
				$result_map put "message" "Stage does not exist"
				$result_map put "responseCode" [java::new Integer 400]
				set response_list [create_error_response $result_map]
				return $response_list	
			} else {
				return $stage
			}
		}
	} else {
		$result_map put "code" "ERR_CONTENT_INVALID_REQUEST"
		$result_map put "message" "Content doesnot exist"
		$result_map put "responseCode" [java::new Integer 400]
		set response_list [create_error_response $result_map]
		return $response_list
	}
	set response_list [create_response $result_map]
	return $response_list
} else {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_CONTENT_INVALID_CONTENT"
	$result_map put "message" "Content is invalid"
	$result_map put "responseCode" [java::new Integer 400]
	set response_list [create_error_response $result_map]
	return $response_list
}
