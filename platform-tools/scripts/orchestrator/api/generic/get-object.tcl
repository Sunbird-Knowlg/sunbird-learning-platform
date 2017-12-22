package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set get_node_response [getDataNode $graph_id $obj_id]
set get_node_response_error [check_response_error $get_node_response]
if {$get_node_response_error} {
	return $get_node_response
}

set resp_def_node [getDefinition $graph_id $object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]

set obj_node [get_resp_value $get_node_response "node"]
set object [convert_graph_node $obj_node $def_node]

set result_map [java::new HashMap]
$result_map put "object" $object
set api_response [create_response $result_map]
return $api_response

