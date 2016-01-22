package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_type "Word"

set map [java::new HashMap]
$map put "nodeType" "DATA_NODE"
$map put "objectType" $object_type
set filter_list [java::new ArrayList]
set filter1 [java::new HashMap]
$filter1 put "property" "syllableCount"
$filter1 put "operator" "="
$filter1 put "value" $syllableCount
$filter_list add $filter1

set filter2 [java::new HashMap]
$filter2 put "property" "lemma"
$filter2 put "operator" "startsWith"
$filter2 put "value" $startsWith
$filter_list add $filter2

set filter3 [java::new HashMap]
$filter3 put "property" "orthographic_complexity"
$filter3 put "operator" ">="
$filter3 put "value" $minComplexity
$filter_list add $filter3

$map put "filters" $filter_list
set search_criteria [create_search_criteria $map]
set search_response [searchNodes $language_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	puts "Error response from searchNodes"
	return $search_response;
} else {
	set result_map [java::new HashMap]
	java::try {
		set graph_nodes [get_resp_value $search_response "node_list"]
		set word_list [java::new ArrayList]
		java::for {Node graph_node} $graph_nodes {
			set metadata [java::prop $graph_node "metadata"]
			set lemma [$metadata get "lemma"]
			$word_list add $lemma
		}
		$result_map put "words" $word_list
	} catch {Exception err} {
    	puts [$err getMessage]
    	$result_map put "error" [$err getMessage]
	}
	set response_list [create_response $result_map]
	return $response_list
}