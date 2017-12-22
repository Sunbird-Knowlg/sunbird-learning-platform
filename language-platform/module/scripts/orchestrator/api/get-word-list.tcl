package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node Relation

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

set lemma_list [java::new ArrayList]
set object_type "WordList"

set map [java::new HashMap]
$map put "nodeType" "SET"
$map put "objectType" $object_type
$map put "identifier" $wordlist_id
set search_criteria [create_search_criteria $map]
set search_response [searchNodes $language_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set result_map [java::new HashMap]
	java::try {
		set graph_nodes [get_resp_value $search_response "node_list"]
		set set_metadata [java::new HashMap]
		set word_id_list [java::new ArrayList]
		java::for {Node graph_node} $graph_nodes {
			set word_ids [getNodeRelationIds $graph_node "Word" "endNodeId"]
			set not_empty_list [isNotEmpty $word_ids]
			if {$not_empty_list} {
				$word_id_list addAll $word_ids
			}
			set set_metadata [java::prop $graph_node "metadata"]
		}
		set set_map [java::new HashMap]
		$set_map put "identifier" $wordlist_id
		$set_map put "metadata" $set_metadata
		
		set map [java::new HashMap]
		$map put "nodeType" "DATA_NODE"
		$map put "objectType" "Word"
		$map put "identifier" $word_id_list
		set search_criteria [create_search_criteria $map]
		set search_response [searchNodes $language_id $search_criteria]
		set check_error [check_response_error $search_response]
		if {$check_error} {
			return $search_response;
		} else {
			set graph_nodes [get_resp_value $search_response "node_list"]	
			set lemma_list [java::new ArrayList]
			java::for {Node graph_node} $graph_nodes {
				set word_metadata [java::prop $graph_node "metadata"]
				$lemma_list add [$word_metadata get "lemma"]
			}
			$set_map put "words" $lemma_list
			$result_map put "wordlist" $set_map
		}

	} catch {Exception err} {
    	$result_map put "error" [$err getMessage]
	}
	set response_list [create_response $result_map]
	return $response_list
}