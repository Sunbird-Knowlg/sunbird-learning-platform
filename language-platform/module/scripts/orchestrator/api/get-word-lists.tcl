package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation

proc getOutRelations {graph_node} {
	set outRelations [java::prop $graph_node "outRelations"]
	return $outRelations
}

proc isNotEmpty {relations} {
	set exist false
	set hasRelations [java::isnull $relations]
	if {$hasRelations == 0} {
		set relationsSize [$relations size] 
		if {$relationsSize > 0} {
			set exist true
		}
	}
	return $exist
}

proc getNodeRelationIds {graph_node relationType property} {

	set relationIds [java::new ArrayList]
	set outRelations [getOutRelations $graph_node]
	set hasRelations [isNotEmpty $outRelations]
	if {$hasRelations} {
		java::for {Relation relation} $outRelations {
			if {[java::prop $relation "endNodeObjectType"] == $relationType} {
				set prop_value [java::prop $relation $property]
				$relationIds add [java::prop $relation $property]
			}
		}
	}
	return $relationIds
}

proc getWordLemmas {language_id word_id_list} {
	set map [java::new HashMap]
	$map put "nodeType" "DATA_NODE"
	$map put "objectType" "Word"
	$map put "identifier" $word_id_list
	set search_criteria [create_search_criteria $map]
	set search_response [searchNodes $language_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		java::throw [java::new Exception "Error response from searchDataNodes"]
	} else {
		set graph_nodes [get_resp_value $search_response "node_list"]	
		set lemma_list [java::new ArrayList]
		java::for {Node graph_node} $graph_nodes {
			set word_metadata [java::prop $graph_node "metadata"]
			$lemma_list add [$word_metadata get "lemma"]
		}
		return $lemma_list
	}
}

set lemma_list [java::new ArrayList]
set object_type "WordList"

set map [java::new HashMap]
$map put "nodeType" "SET"
$map put "objectType" $object_type
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
		set word_list_map [java::new HashMap]
		java::for {Node graph_node} $graph_nodes {
			set set_id [java::prop $graph_node "identifier"]
			set set_map [java::new HashMap]
			$set_map put "identifier" $set_id
			$set_map put "metadata" [java::prop $graph_node "metadata"]
			set word_ids [getNodeRelationIds $graph_node "Word" "endNodeId"]
			set not_empty_list [isNotEmpty $word_ids]
			if {$not_empty_list} {
				set lemma_list [getWordLemmas $language_id $word_ids]
				$set_map put "words" $lemma_list
			}
			$word_list_map put $set_id $set_map
		}
		$result_map put "wordlists" $word_list_map
	} catch {Exception err} {
    	puts [$err getMessage]
    	$result_map put "error" [$err getMessage]
	}
	set response_list [create_response $result_map]
	return $response_list
}