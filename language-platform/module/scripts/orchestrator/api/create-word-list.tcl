package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set lemma_list [java::new ArrayList]
set object_type "Word"
set set_type "WordList"
set error_status "Failed"

set contains_response [containsLanguage $language_id]
set contains_response_error [check_response_error $contains_response]
if {$contains_response_error} {
	puts "Error response from containsLanguage"
	return $contains_response;
}
set result [$contains_response get "result"]
set lang_eqs [$result equals "true"]
if {!$lang_eqs} {
	set result_map [java::new HashMap]
	$result_map put "code" "INVALID_LANGUAGE"
	$result_map put "message" "INVALID LANGUAGE"
	$result_map put "responseCode" [java::new Integer 400]
	set err_response [create_error_response $result_map]
	return $err_response
}
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
	set graphSize [$graph_nodes size]
	set lemmaSize [$words size]
	puts "graph_nodes size is $graphSize"
	if {$graphSize < $lemmaSize} {
		set result_map [java::new HashMap]
		$result_map put "code" "NODE_NOT_FOUND"
		$result_map put "message" "Node(s) not found"
		$result_map put "responseCode" [java::new Integer 400]
		set err_response [create_error_response $result_map]
		return $err_response
	}	
	java::for {Node graph_node} $graph_nodes {
		set word_id [java::prop $graph_node "identifier"]
		puts "word id is $word_id"
		$word_id_list add $word_id
	}
	set set_node [java::new Node]
	java::prop $set_node "metadata" $metadata
	set resp [createSet $language_id $word_id_list $set_type $object_type $set_node]
	return $resp
}
