package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_type "Language"
set graph_id "domain"

set map [java::new HashMap]
$map put "nodeType" "DATA_NODE"
$map put "objectType" $object_type
set search_criteria [create_search_criteria $map]
set search_response [searchNodes $graph_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set result_map [java::new HashMap]
	java::try {
		set graph_nodes [get_resp_value $search_response "node_list"]
		set language_list [java::new ArrayList]
		java::for {Node graph_node} $graph_nodes {
			set lang_id [java::prop $graph_node "identifier"]
			set metadata [java::prop $graph_node "metadata"]
			set lang_map [java::new HashMap]
			$lang_map put "identifier" $lang_id
			$lang_map putAll $metadata
			$language_list add $lang_map
		}
		$result_map put "languages" $language_list
	} catch {Exception err} {
    	$result_map put "error" [$err getMessage]
	}
	set response_list [create_response $result_map]
	return $response_list
}