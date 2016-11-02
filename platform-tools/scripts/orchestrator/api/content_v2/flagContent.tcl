package require java
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set graph_id "domain"
set object_type "Content"

set resp_get_node [getDataNode $graph_id $contentId]
set check_error [check_response_error $resp_get_node]
if {$check_error} {
	return $resp_get_node;
} else {
	set graph_node [get_resp_value $resp_get_node "node"]
	set node_object_type [java::prop $graph_node "objectType"]
	if {$node_object_type == $object_type} {
		set node_metadata [java::prop $graph_node "metadata"]
		set status_val [$node_metadata get "status"]
		set status_val_str [java::new String [$status_val toString]]
		set isLiveState [$status_val_str equalsIgnoreCase "Live"]
		if {$isLiveState == 1} {
			$node_metadata put "status" "Flagged"
			set create_response [updateDataNode $graph_id $contentId $graph_node]
			return $create_response
		} else {
			set result_map [java::new HashMap]
			$result_map put "code" "ERR_CONTENT_NOT_FLAGGABLE"
			$result_map put "message" "Unpublished Content $contentId cannot be flagged"
			$result_map put "responseCode" [java::new Integer 400]
			set response_list [create_error_response $result_map]
			return $response_list
		}
	} else {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_NODE_NOT_FOUND"
		$result_map put "message" "$object_type $contentId not found"
		$result_map put "responseCode" [java::new Integer 404]
		set response_list [create_error_response $result_map]
		return $response_list
	}
}