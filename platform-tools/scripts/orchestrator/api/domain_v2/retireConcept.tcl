package require java
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set graph_id "domain"
set object_type "Concept"

set resp_get_node [getDataNode $graph_id $conceptId]
set check_error [check_response_error $resp_get_node]
if {$check_error} {
	return $resp_get_node;
} else {
	set graph_node [get_resp_value $resp_get_node "node"]
	set node_object_type [java::prop $graph_node "objectType"]
	if {$node_object_type == $object_type} {
		set node_metadata [java::prop $graph_node "metadata"]
		set node_subject [$node_metadata get "subject"]	
		set node_subject_str [$node_subject toString]	
		if {$domainId == $node_subject_str} {
			$node_metadata put "status" "Retired"
			set create_response [updateDataNode $graph_id $conceptId $graph_node]
			return $create_response
		} else {
			set result_map [java::new HashMap]
			$result_map put "code" "ERR_NODE_NOT_FOUND"
			$result_map put "message" "$object_type $conceptId not found in $domainId"
			$result_map put "responseCode" [java::new Integer 404]
			set response_list [create_error_response $result_map]
			return $response_list
		}
	} else {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_NODE_NOT_FOUND"
		$result_map put "message" "$object_type $conceptId not found"
		$result_map put "responseCode" [java::new Integer 404]
		set response_list [create_error_response $result_map]
		return $response_list
	}
}