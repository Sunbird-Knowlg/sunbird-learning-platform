package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set object_type "Varna"
set error_status "Failed"

set get_node_response [getDataNode $language_id $varna_id]
set get_node_response_error [check_response_error $get_node_response]
if {$get_node_response_error} {
	return $get_node_response
}

set resp_def_node [getDefinition $language_id $object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]

set varna_node [get_resp_value $get_node_response "node"]
set varna_obj [convert_graph_node $varna_node $def_node]

set result_map [java::new HashMap]
$result_map put "varna" $varna_obj
set api_response [create_response $result_map]
return $api_response

