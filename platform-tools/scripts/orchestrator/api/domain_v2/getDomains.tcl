package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set is_sortBy_null [java::isnull $sortBy]
set is_resultSize_null [java::isnull $resultSize]
set object_type "Domain"
set map [java::new HashMap]
$map put "objectType" $object_type
$map put "nodeType" "DATA_NODE"
if {$is_sortBy_null == 0} {
	$map put "sortBy" $sortBy
}
if {$is_resultSize_null == 0} {
	$map put "resultSize" $resultSize
}
$map put "order" $order
set search_criteria [create_search_criteria $map]

set graph_id "domain"
set search_response [searchNodes $graph_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	puts "Error response from searchNodes"
	return $search_response;
} else {
	set graph_nodes [get_resp_value $search_response "node_list"]
	set resp_def_node [getDefinition $graph_id $object_type]
	set def_node [get_resp_value $resp_def_node "definition_node"]
	set obj_list [java::new ArrayList]
	java::for {Node graph_node} $graph_nodes {
		set domain_obj [convert_graph_node $graph_node $def_node]
		$obj_list add $domain_obj
	}
	set result_map [java::new HashMap]
	$result_map put "domains" $obj_list
	set response_list [create_response $result_map]
	return $response_list
}