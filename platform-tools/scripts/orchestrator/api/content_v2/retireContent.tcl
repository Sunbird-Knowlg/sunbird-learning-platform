package require java
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set graph_id "domain"
set object_type "Content"
set image_object_type "ContentImage"

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
		$node_metadata put "status" "Retired"
		set create_response [updateDataNode $graph_id $contentId $graph_node]

		set content_image_id ${contentId}.img
		set resp_get_image_node [getDataNode $graph_id $content_image_id]
		set check_error [check_response_error $resp_get_image_node]
		if {$check_error} {
		} else {
			set graph_image_node [get_resp_value $resp_get_image_node "node"]		
			set image_node_metadata [java::prop $graph_image_node "metadata"]
			set request [java::new HashMap]
			$request put "versionKey" [$image_node_metadata get "versionKey"]
			$request put "status" "Retired"
			$request put "objectType" $image_object_type
			$request put "identifier" $content_image_id
			set resp_def_node [getDefinition $graph_id $image_object_type]
			set def_node [get_resp_value $resp_def_node "definition_node"]
			set domain_obj [convert_to_graph_node $request $def_node]
			set create_image_response [updateDataNode $graph_id $content_image_id $domain_obj]
		}
		return $create_response
	} else {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_NODE_NOT_FOUND"
		$result_map put "message" "$object_type $contentId not found"
		$result_map put "responseCode" [java::new Integer 404]
		set response_list [create_error_response $result_map]
		return $response_list
	}
}