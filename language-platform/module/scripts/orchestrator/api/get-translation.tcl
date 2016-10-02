package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation

proc getOutRelations {graph_node} {
	set outRelations [java::prop $graph_node "outRelations"]
	return $outRelations
}

proc getInRelations {graph_node} {
	set inRelations [java::prop $graph_node "inRelations"]
	return $inRelations
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

proc getNodeRelationIds {graph_node relationType property languages} {

	set relationIds [java::new ArrayList]
	set outRelations [getOutRelations $graph_node]
	set hasRelations [isNotEmpty $outRelations]
	if {$hasRelations} {
		
		java::for {Relation relation} $outRelations {
			set index 0
			if {[java::prop $relation "endNodeObjectType"] == $relationType} {
				puts "entering if"
				set prop_value [java::prop $relation $property]
				puts $prop_value
				set idArray [split $prop_value ":"]
				puts "split successfully"
				foreach entry $idArray {
				 set languageContains [$languages contains $entry]
					if {$languageContains == 1} {
					puts "yes"
					$relationIds add $prop_value
				 }
				 set index [expr $index + 1]
				}
				puts $index
				if {$index == 1} {
					set idArray [split $prop_value "_"]
					foreach entry $idArray {
					 set languageContains [$languages contains $entry]
						if {$languageContains == 1} {
						$relationIds add $prop_value
				 }
				 }
				}
					
			}
		}
	}
	return $relationIds
}

proc getInNodeRelationIds {graph_node relationType property} {

	set relationIds [java::new ArrayList]
	set inRelations [getInRelations $graph_node]
	set hasRelations [isNotEmpty $inRelations]
	if {$hasRelations} {
		java::for {Relation relation} $inRelations {
			if {[java::prop $relation "startNodeObjectType"] == $relationType} {
				puts "getting synset id"
				set prop_value [java::prop $relation $property]
				$relationIds add $prop_value
			}
		}
	}
	return $relationIds
}

set object_type "TranslationSet"
set node_id $word_id
set language_id $language_id
set get_node_response [getDataNode $language_id $node_id]
set get_node_response_error [check_response_error $get_node_response]
if {$get_node_response_error} {
	return $get_node_response
}


set word_node [get_resp_value $get_node_response "node"]
set synonym_list [getInNodeRelationIds $word_node "Synset" "startNodeId"]
set synset_list [java::new ArrayList]
$synset_list addAll $synonym_list

set relationMap [java::new HashMap]
$relationMap put "name" "hasMember"
$relationMap put "objectType" "Synset"
$relationMap put "identifiers" $synset_list

set criteria_list [java::new ArrayList]
$criteria_list add $relationMap

set criteria_map [java::new HashMap]
$criteria_map put "nodeType" "SET"
$criteria_map put "objectType" $object_type
$criteria_map put "relationCriteria" $criteria_list

set graph_id "translations"

set search_criteria [create_search_criteria $criteria_map]
set search_response [searchNodes $graph_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set result_map [java::new HashMap]
	set result_list [java::new ArrayList]
	java::try {
		set graph_nodes [get_resp_value $search_response "node_list"]
		set synset_id_list [java::new ArrayList]
		java::for {Node graph_node} $graph_nodes {
		puts "nodes found"
			set synset_ids [getNodeRelationIds $graph_node "Synset" "endNodeId" $languages]
			set not_empty_list [isNotEmpty $synset_ids]
			if {$not_empty_list} {
				$synset_id_list addAll $synset_ids
				set searchResponse [multiLanguageWordSearch $synset_id_list]
				set searchResultsMap [$searchResponse get "translations"]
				$result_list add $searchResultsMap
			}
	
		}
		$result_map put "translations" $result_list

	} catch {Exception err} {
    	$result_map put "error" [$err getMessage]
	}
	set response_list [create_response $result_map]
	return $response_list
}