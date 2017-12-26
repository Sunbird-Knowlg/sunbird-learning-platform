package require java
java::import -package java.util HashMap Map
java::import -package java.util ArrayList List
java::import -package org.ekstep.graph.dac.model Node


set object_type "Word"
set map [java::new HashMap]
$map put "objectType" $object_type
$map put "nodeType" "DATA_NODE"
$map put "status" "Live"

set search_criteria [create_search_criteria $map]
set search_response [searchNodes $language_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set graph_nodes [get_resp_value $search_response "node_list"]
	set list [java::new ArrayList]
	java::for {Node node} $graph_nodes {
		set identifier [java::prop $node "identifier"]
		set metadata [java::prop $node "metadata"]
		set map [java::new HashMap]
		$map put "identifier" $identifier
		$map put "lemma" [$metadata get "lemma"]
		$map put "primaryMeaningId" [$metadata get "primaryMeaningId"]
		$list add $map
	}
	return $list
}