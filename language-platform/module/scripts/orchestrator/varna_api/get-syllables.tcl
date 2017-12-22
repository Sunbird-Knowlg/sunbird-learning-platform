package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set varna_object_type "Varna"
set varna_iso_object_type "Varna_ISO"
set varna_iso_graph_id "language"
set object_null [java::isnull $word]
if {$object_null == 1} {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_INVALID_REQUEST"
	$result_map put "message" "Word IS MANDATORY"
	$result_map put "responseCode" [java::new Integer 400]
	set response_list [create_error_response $result_map]
	return $response_list
} 

set language_id [get_language_graph_id $word]
set syllables [get_syllables_word $language_id $word]

return $syllables


