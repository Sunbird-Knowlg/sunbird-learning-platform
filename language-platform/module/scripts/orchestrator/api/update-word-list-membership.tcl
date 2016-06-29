package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation

proc proc_isNotEmpty {relations} {
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

set object_type "Word"
set map [java::new HashMap]
$map put "nodeType" "DATA_NODE"
$map put "objectType" $object_type
$map put "identifier" $wordIds

set search_criteria [create_search_criteria $map]
set search_response [searchNodes $language_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set graph_nodes [get_resp_value $search_response "node_list"]
	java::for {Node graph_node} $graph_nodes {
		set wordId [java::prop $graph_node "identifier"]
		set wordLists [java::new ArrayList]
		set wordListIds [java::new ArrayList]
		set inRelations [java::prop $graph_node "inRelations"]
		set hasRelations [proc_isNotEmpty $inRelations]
		if {$hasRelations} {
			java::for {Relation relation} $inRelations {
				if {[java::prop $relation "startNodeObjectType"] == "WordList" && [java::prop $relation "relationType"] == "hasMember"} {
					set wordListId [java::prop $relation "startNodeId"]
					$wordListIds add $wordListId
					set start_node_metadata [java::prop $relation "startNodeMetadata"]
					set hasMetadata [proc_isNotEmpty $start_node_metadata]
					if {$hasMetadata} {
						set word_list_name [$start_node_metadata get "name"]
						set hasWordListName [java::isnull $word_list_name]
						if {$hasWordListName == 0} {
							$wordLists add $word_list_name
						}
					}
				}
			}
		}
		set metadata [java::prop $graph_node "metadata"]
		$metadata put "wordLists" $wordLists
		$metadata put "wordListIds" $wordListIds
		java::prop $graph_node "metadata" $metadata
		updateDataNode $language_id $wordId $graph_node
	}	
	set result_map [java::new HashMap]
	$result_map put "status" "OK"
	set response_list [create_response $result_map]
	return $response_list
}
