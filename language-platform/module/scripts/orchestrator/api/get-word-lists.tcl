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

proc getMemberLemmas {graph_node} {
	set relationIds [java::new ArrayList]
	set outRelations [getOutRelations $graph_node]
	set hasRelations [isNotEmpty $outRelations]
	if {$hasRelations} {
		java::for {Relation relation} $outRelations {
			if {[java::prop $relation "endNodeObjectType"] == "Word"} {
				set end_node_metadata [java::prop $relation "endNodeMetadata"]
				set hasMetadata [isNotEmpty $end_node_metadata]
				if {$hasMetadata} {
					$relationIds add [$end_node_metadata get "lemma"]
				}
			}
		}
	}
	return $relationIds
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
			set lemma_list [getMemberLemmas $graph_node]
			set not_empty_list [isNotEmpty $lemma_list]
			if {$not_empty_list} {
				$set_map put "words" $lemma_list
			}
			$word_list_map put $set_id $set_map
		}
		$result_map put "wordlists" $word_list_map
	} catch {Exception err} {
    	$result_map put "error" [$err getMessage]
	}
	set response_list [create_response $result_map]
	return $response_list
}