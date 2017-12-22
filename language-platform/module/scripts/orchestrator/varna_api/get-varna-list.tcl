package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set search [java::new HashMap]
set varna_object_type "Varna"
$search put "objectType" $varna_object_type
$search put "nodeType" "DATA_NODE"

set type_list [java::new ArrayList]
if {$language_id == "en"} {
	$type_list add "Alphabet"
} else {
	$type_list add "Vowel"
	$type_list add "Consonant"
}
$search put "type" $type_list

set sort_by [java::new ArrayList]
$sort_by add "varna"
$search put "sortBy" $sort_by
$search put "order" "ASC"

set search_criteria [create_search_criteria $search]
set search_response [searchNodes $language_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response
} else {
	set varna_list [java::new ArrayList]
	set graph_nodes [get_resp_value $search_response "node_list"]
	java::for {Node graph_node} $graph_nodes {
		set node_id [java::prop $graph_node "identifier"]
		$varna_list add $node_id
	}
	return $varna_list
}
