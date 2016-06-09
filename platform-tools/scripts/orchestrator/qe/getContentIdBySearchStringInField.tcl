package require java
java::import -package java.util HashMap Map
java::import -package java.util ArrayList List
java::import -package com.ilimi.graph.dac.model Node

set graph_id "domain"
set object_type "Content"
set search [java::new HashMap]
$search put "objectType" $object_type
$search put "nodeType" "DATA_NODE"


set filter_list [java::new ArrayList]
set filter1 [java::new HashMap]
$filter1 put "property" $searchProperty
$filter1 put "operator" $searchOperator
$filter1 put "value" $searchString
$filter_list add $filter1

$search put "filters" $filter_list
set search_criteria [create_search_criteria $search]
set search_response [searchNodes $graph_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	puts "Error response from searchNodes"
	return $search_response;
} else {
	set item_list [java::new ArrayList]
  set result_map [java::new HashMap]
  set count 0
	set graph_nodes [get_resp_value $search_response "node_list"]
	java::for {Node graph_node} $graph_nodes {
		set itemId [java::prop $graph_node "identifier"]
		$item_list add $itemId
    set count [expr {$count+1}]
	}
  $result_map put "content_count" $count
  $result_map put "content_ids" $item_list
	return $result_map
}
