package require java
java::import -package java.util HashMap Map

set object_type "Content"
set graph_id "domain"
set resp_get_node [getDataNode $graph_id $content_id]
set check_error [check_response_error $resp_get_node]
if {$check_error} {
	return $resp_get_node;
} else {
	set graph_node [get_resp_value $resp_get_node "node"]
	set resp_def_node [getDefinition $graph_id $object_type]
	set def_node [get_resp_value $resp_def_node "definition_node"]
	set resp_object [convert_graph_node $graph_node $def_node]
	set result_map [java::new HashMap]
	$result_map put "content" $resp_object
	set response_list [create_response $result_map]
	return $response_list
}