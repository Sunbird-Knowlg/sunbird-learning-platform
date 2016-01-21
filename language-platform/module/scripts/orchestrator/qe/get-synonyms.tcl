package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package com.ilimi.graph.dac.model Node Relation

proc getRelations {graph_node relations_dir} {

	set outRelations [java::prop $graph_node $relations_dir]
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

proc getRelationIds {graph_node relationDir relationType compare_prop property} {

	set relationIds [java::new ArrayList]
	set inRelations [getRelations $graph_node $relationDir]
	set hasRelations [isNotEmpty $inRelations]
	if {$hasRelations} {
		java::for {Relation relation} $inRelations {
			if {[java::prop $relation $compare_prop] == $relationType} {
				set prop_value [java::prop $relation $property]
				$relationIds add [java::prop $relation $property]
			}
		}
	}
	return $relationIds
}

proc getInRelationIds {graph_node relationType compare_prop property} {

	set relationIds [getRelationIds $graph_node "inRelations" $relationType $compare_prop $property]
	return $relationIds
}

proc getOutRelationIds {graph_node relationType compare_prop property} {

	set relationIds [getRelationIds $graph_node "outRelations" $relationType $compare_prop $property]
	return $relationIds
}

proc searchQuery {language_id map} {

	set search_criteria [create_search_criteria $map]
	set search_response [searchNodes $language_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		puts "Error response from searchNodes"
		java::throw [java::new Exception "Error response from searchNodes"]
	} else {
		set graph_nodes [get_resp_value $search_response "node_list"]
		return $graph_nodes
	}
}

proc getNode {list} {
	set node [java::new Node]
	set not_empty [isNotEmpty $list]
	if {$not_empty} {
		java::for {Node graph_node} $list {
			set node $graph_node
		}
	}
	return $node
}

set result_map [java::new HashMap]
java::try {
	set map [java::new HashMap]
	$map put "nodeType" "DATA_NODE"
	$map put "objectType" "Word"
	$map put "lemma" $word
	set graph_nodes [searchQuery $language_id $map]
	set not_empty [isNotEmpty $graph_nodes]
	set synonyms [java::new HashSet]
	if {$not_empty} {
		set graph_node [getNode $graph_nodes]
		set synset_ids [getInRelationIds $graph_node "Synset" "startNodeObjectType" "startNodeId"]
		set not_empty_list [isNotEmpty $synset_ids]
		if {$not_empty_list} {
			set word_id_list [java::new ArrayList]
			set synset_id_list [java::new ArrayList]
			$synset_id_list addAll $synset_ids
			set map [java::new HashMap]
			$map put "nodeType" "DATA_NODE"
			$map put "objectType" "Synset"
			$map put "identifier" $synset_id_list
			set synset_nodes [searchQuery $language_id $map]
			java::for {Node graph_node} $synset_nodes {
				set word_ids [getOutRelationIds $graph_node "Word" "endNodeObjectType" "endNodeId"]
				$word_id_list addAll $word_ids
			}

			set map [java::new HashMap]
			$map put "nodeType" "DATA_NODE"
			$map put "objectType" "Word"
			$map put "identifier" $word_id_list
			set word_nodes [searchQuery $language_id $map]
			java::for {Node graph_node} $word_nodes {
				set metadata [java::prop $graph_node "metadata"]
				set lemma [$metadata get "lemma"]
				$synonyms add $lemma
			}
		}
	}
	$result_map put "synonyms" $synonyms
} catch {Exception err} {
	puts [$err getMessage]
	$result_map put "error" [$err getMessage]
}
set response_list [create_response $result_map]
return $response_list