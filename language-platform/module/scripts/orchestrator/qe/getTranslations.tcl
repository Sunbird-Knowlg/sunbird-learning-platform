package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package com.ilimi.graph.dac.model Node Relation
java::import -package org.codehaus.jackson.map ObjectMapper

proc proc_getTranslations {translations mapper} {
	set trans_map [java::new HashMap]
	set object_null [java::isnull $translations]
	if {$object_null == 1} {
		return $trans_map
	} else {
		set map_class [$trans_map getClass]
		java::try {
			set str_translations [$translations toString]
			set trans_object [$mapper readValue $str_translations $map_class]
			set trans_map [java::cast HashMap $trans_object]
		} catch {Exception err} {
	    	puts "Exception: $err"
		}
		return $trans_map
	}
}

set search [java::new HashMap]
$search put "objectType" "Synset"
$search put "nodeType" "DATA_NODE"

set search_criteria [create_search_criteria $search]
set search_response [searchNodes $language_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	puts "Error response from searchNodes"
	return $search_response;
} else {
	set word_list [java::new HashSet]
	set mapper [java::new ObjectMapper]
	set graph_nodes [get_resp_value $search_response "node_list"]
	java::for {Node graph_node} $graph_nodes {
		set metadata [java::prop $graph_node "metadata"]
		set translations [$metadata get "translations"]
		set trans_map [proc_getTranslations $translations $mapper]
		set trans_list_obj [$trans_map get $languageCode]
		set list_null [java::isnull $trans_list_obj]
		if {$list_null != 1} {
			set trans_list [java::cast ArrayList $trans_list_obj]
			$word_list addAll $trans_list
		}
	}
	set result_map [java::new HashMap]
	$result_map put "words" $word_list
	set response_list [create_response $result_map]
	return $response_list
}



