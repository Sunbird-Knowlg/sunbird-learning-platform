package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set lemma_list [java::new ArrayList]
set object_type "Word"
set set_type "WordList"

puts "words"

set map [java::new HashMap]
$map put "nodeType" "DATA_NODE"
$map put "objectType" $object_type
$map put "lemma" $words
set search_criteria [create_search_criteria $map]
set search_response [searchNodes $language_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	puts "Error response from searchNodes"
	return $search_response;
} else {
	set graph_nodes [get_resp_value $search_response "node_list"]
	set word_id_list [java::new ArrayList]
	java::for {Node graph_node} $graph_nodes {
		set word_id [java::prop $graph_node "identifier"]
		$word_id_list add $word_id
	}
	set set_node [java::new Node]
	java::prop $set_node "metadata" $metadata
	set resp [createSet $language_id $word_id_list $set_type $object_type $set_node]
	return $resp
}