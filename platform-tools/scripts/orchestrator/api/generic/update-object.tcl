package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_null [java::isnull $object]
if {$object_null == 1} {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_CONTENT_INVALID_OBJECT"
	$result_map put "message" "Invalid Request"
	$result_map put "responseCode" [java::new Integer 400]
	set update_obj_error_response [create_error_response $result_map]
	return $update_obj_error_response
} else {
	set resp_def_node [getDefinition $graph_id $object_type]
	set def_node [get_resp_value $resp_def_node "definition_node"]
	$object put "objectType" $object_type
	set node_obj [convert_to_graph_node $object $def_node]
	set update_response [updateDataNode $graph_id $obj_id $node_obj]
	return $update_response
}
