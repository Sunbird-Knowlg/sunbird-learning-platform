package require java
java::import -package java.util HashMap Map
java::import -package java.util ArrayList List
java::import -package com.ilimi.graph.dac.model Node

set graph_id "domain"
set object_type "Content"
set search [java::new HashMap]
$search put "objectType" $object_type
$search put "nodeType" "DATA_NODE"
$search put "status" "Retired"

set search_criteria [create_search_criteria $search]
set search_response [searchNodes $graph_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	puts "Error response from searchNodes"
	return $search_response;
} else {
  set item_info [java::new HashMap]
	set item_list [java::new ArrayList]
  set retired_content_count 0
	set graph_nodes [get_resp_value $search_response "node_list"]
	java::for {Node graph_node} $graph_nodes {
		set itemId [java::prop $graph_node "identifier"]
		puts "deleting item: $itemId"
		$item_list add $itemId
    set retired_content_count [expr {$retired_content_count+1}]
	}
  $item_info put "retired_content_ids" $item_list
  $item_info put "retired_content_count" $retired_content_count
	return $item_info
}
