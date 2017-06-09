package require java
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set graph_id "domain"
set object_type "Content"


set resp_get_node [getDataNode $graph_id $content_id]
set check_error [check_response_error $resp_get_node]
if {$check_error} {
	return $resp_get_node
} else {
	set graph_node [get_resp_value $resp_get_node "node"]
	set node_object_type [java::prop $graph_node "objectType"]
	if {$node_object_type == $object_type} {
		set node_metadata [java::prop $graph_node "metadata"]
		set status_val [$node_metadata get "status"]
		set status_val_str [java::new String [$status_val toString]]
		set isFlaggedstate [$status_val_str equalsIgnoreCase "Flagged"]
		if {$isFlaggedstate == 1} {
			set request [java::new HashMap]
			$request put "flagReasons" [java::null]
			$request put "versionKey" [$node_metadata get "versionKey"]
			$request put "status" "Live"
			$request put "objectType" $object_type
			$request put "identifier" $content_id
			set resp_def_node [getDefinition $graph_id $object_type]
			set def_node [get_resp_value $resp_def_node "definition_node"]
			set domain_obj [convert_to_graph_node $request $def_node]
			set create_response [updateDataNode $graph_id $content_id $domain_obj]
                        return $create_response
		} else {
			set result_map [java::new HashMap]
			$result_map put "code" "ERR_CONTENT_NOT_FLAGGED"
			$result_map put "message" "Content $content_id is not flagged to reject"
			$result_map put "responseCode" [java::new Integer 400]
			set response_list [create_error_response $result_map]
			return $response_list
		}
	} else {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_NODE_NOT_FOUND"
		$result_map put "message" "$object_type $content_id not found"
		$result_map put "responseCode" [java::new Integer 404]
		set response_list [create_error_response $result_map]
		return $response_list
	}
}
