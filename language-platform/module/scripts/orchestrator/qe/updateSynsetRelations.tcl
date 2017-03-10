package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation


proc isNotEmpty {list} {
	set exist false
	set isEmpty [java::isnull $list]
	if {$isEmpty == 0} {
		set listSize [$list size] 
		if {$listSize > 0} {
			set exist true
		}
	}
	return $exist
}


proc getSynset { language_id startPosition resultSize } {

	set object_type "Synset"
	set map [java::new HashMap]
	$map put "objectType" $object_type
	$map put "nodeType" "DATA_NODE"
	$map put "startPosition" [java::new Integer $startPosition]
	$map put "resultSize" [java::new Integer $resultSize]

	set search_criteria [create_search_criteria $map]
	set synsets [java::new ArrayList]

	set search_response [searchNodes $language_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
	} else {
		set graph_nodes [get_resp_value $search_response "node_list"]
		set synsets [java::cast List $graph_nodes]
	}

	return $synsets

}


proc createSynsetNode { language_id gloss } {

	set synsetMetaData [java::new HashMap]
	$synsetMetaData put "gloss" $gloss
	set response [createSynset $language_id $synsetMetaData]
	return $response
}

proc correctSynset {language_id synset } {
		# get inRelations of given word
		set synsetIdentifier [java::prop $synset "identifier"]
		set outRelations [java::prop $synset "outRelations"]
		set hasRelations [isNotEmpty $outRelations]
		if {$hasRelations} {
			java::for {Relation relation} $outRelations {
				set relationType [java::prop $relation "relationType"]
				set endNodeId [java::prop $relation "endNodeId"]
				set endNodeName [java::prop $relation "endNodeName"]
				if {($relationType != "synonym") && ($endNodeName == "Word") } {
					set resp_get_node [getDataNode $language_id $endNodeId]
					set check_error [check_response_error $resp_get_node]
					if {$check_error} {
						return $resp_get_node;
					} else {
						set graph_node [get_resp_value $resp_get_node "node"]
						set node_metadata [java::prop $graph_node "metadata"]
						set node_identifier [java::prop $graph_node "identifier"]
						set nodePrimaryMeaningId [$node_metadata get "primaryMeaningId"]
						set primaryMeaningIdNull [java::isnull $nodePrimaryMeaningId]
						if {$primaryMeaningIdNull == 1} {
							set nodeLemma [$node_metadata get "lemma"]
							set synsetResponse [createSynsetNode $language_id $nodeLemma]
							set check_error [check_response_error $synsetResponse]
							if {$check_error} {
								return $synsetResponse
							} 

							set nodePrimaryMeaningId [get_resp_value $synsetResponse "node_id"]
							set nodePrimaryMeaningId [$nodePrimaryMeaningId toString]
							$node_metadata put "primaryMeaningId" $nodePrimaryMeaningId
							set wordResponse [updateDataNode $language_id $node_identifier $graph_node]
							set check_error [check_response_error $wordResponse]
							if {$check_error} {
								return $wordResponse
							}

							set addRelation_response [addRelation $language_id $nodePrimaryMeaningId "synonym" $node_identifier]
							set check_addRelation_error [check_response_error $addRelation_response]
							if {$check_addRelation_error} {
								return $addRelation_response;
							}

						}

						set deleteRelation_response [deleteRelation $language_id $synsetIdentifier $relationType $node_identifier]
						set check_deleteRelation_error [check_response_error $deleteRelation_response]
						if {$check_deleteRelation_error} {
							return $deleteRelation_response;
						}

						set addRelation_response [addRelation $language_id $synsetIdentifier $relationType $nodePrimaryMeaningId]
						set check_addRelation_error [check_response_error $addRelation_response]
						if {$check_addRelation_error} {
							return $addRelation_response;
						}

					}
				}
			}
		}
		return [java::null]
}

set startPosition 0
set resultSize 1000
set continue true

while {$continue} {
	
	set synsets [getSynset $language_id $startPosition $resultSize]
	puts "synsets size : [$synsets size], startPosition $startPosition ,resultSize $resultSize"
	set hasSynsets [isNotEmpty $synsets]
	if {$hasSynsets} {
		java::for {Node synset} $synsets {
			set id [java::prop $synset "identifier"]
			set correctSynsetResponse [correctSynset $language_id $synset]
			set correctSynsetResponseNull [java::isnull $correctSynsetResponse]
			if {$correctSynsetResponseNull == 0} {
				set errorMsgMap [java::prop $correctSynsetResponse "result"]
				puts "updateSynsetRelations exception while correcting Synset -$id , error [$errorMsgMap toString]"
				return $correctSynsetResponse
			} 
		}

	} else {
		set continue false
	}
	set startPosition [expr $startPosition + $resultSize]
}

