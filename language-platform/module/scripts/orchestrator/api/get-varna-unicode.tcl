package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set varna_object_type "Varna"
set varna_ipa_object_type "Varna_IPA"
set object_null [java::isnull $unicode]
if {$object_null == 1} {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_INVALID_REQUEST"
	$result_map put "message" "UNICODE IS MANDATORY"
	$result_map put "responseCode" [java::new Integer 400]
	set response_list [create_error_response $result_map]
	return $response_list
} 


set searchProperty [java::new HashMap]
$searchProperty put "unicode" $unicode

set property [create_search_property $searchProperty]
set search_response [searchNodes $language_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	puts "Error response from searchNodes"
	return $search_response;
} else {
	set graph_nodes [get_resp_value $search_response "node_list"]
	set resp_def_node [getDefinition $language_id $object_type]
	set def_node [get_resp_value $resp_def_node "definition_node"]
	set obj_list [java::new ArrayList]
	java::for {Node graph_node} $graph_nodes {
		if {$returnFields} {
			set synset_obj [convert_graph_node $graph_node $def_node $fieldList]
		} else {
			set synset_obj [convert_graph_node $graph_node $def_node]
		}
		$obj_list add $synset_obj
	}
	set result_map [java::new HashMap]
	$result_map put "content" $obj_list
	set response_list [create_response $result_map]
	return $response_list
}


