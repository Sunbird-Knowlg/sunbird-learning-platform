package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node Relation

proc isNotNull {value} {
	set exist false
	java::try {
		set hasValue [java::isnull $value]
		if {$hasValue == 0} {
			set exist true
		}
	} catch {Exception err} {
    	set exist false
	}
	return $exist
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

proc procGetOutRelations {graph_node} {
	set outRelations [java::prop $graph_node "outRelations"]
	return $outRelations
}

proc procUpdateRelatedSynsetList {relation wordMap type} {
	set relatedList [$wordMap get $type]
	set relatedListNotNull [isNotNull $relatedList]
	if {!$relatedListNotNull} {
		set relatedList [java::new ArrayList]
	}
	set list [java::cast List $relatedList]
	$list add [java::prop $relation "endNodeId"]
	$wordMap put $type $list
	return $wordMap
}

proc procGetRelatedSynSetId {relation wordMap} {
	if {[java::prop $relation "endNodeObjectType"] == "Synset"} {
		if {[java::prop $relation "relationType"] == "hasHypernym"} {
			set wordMap [procUpdateRelatedSynsetList $relation $wordMap "Hypernyms"]
		} elseif {[java::prop $relation "relationType"] == "hasHyponym"} {
			set wordMap [procUpdateRelatedSynsetList $relation $wordMap "Hyponyms"]
		} elseif {[java::prop $relation "relationType"] == "hasHolonym"} {
			set wordMap [procUpdateRelatedSynsetList $relation $wordMap "Holonyms"]
		} elseif {[java::prop $relation "relationType"] == "hasMeronym"} {
			set wordMap [procUpdateRelatedSynsetList $relation $wordMap "Meronyms"]
		} elseif {[java::prop $relation "relationType"] == "hasAntonym"} {
			set wordMap [procUpdateRelatedSynsetList $relation $wordMap "Antonyms"]
		}
	} elseif {[java::prop $relation "endNodeObjectType"] == "Word"} {
		if {[java::prop $relation "relationType"] == "synonym"} {
			set nodeMetadata [java::prop $relation "endNodeMetadata"]
			set checkMetadata [isNotNull $nodeMetadata]
			if {$checkMetadata} {
				set member_lemma [$nodeMetadata get "lemma"]
				set checkLemma [isNotNull $member_lemma]
				if {$checkLemma} {
					set relatedList [$wordMap get "Words"]	
					set relatedListNotNull [isNotNull $relatedList]
					if {!$relatedListNotNull} {
						set relatedList [java::new ArrayList]
					}
					set list [java::cast List $relatedList]
					$list add $member_lemma
					$wordMap put "Words" $list
				}
			}
		}
	}
	return $wordMap
}

proc procGetSynsetMetadata {node} {
	set wordMap [java::new HashMap]
	set isNodeNotNull [isNotNull $node]
	if {$isNodeNotNull} {
		set metadata [java::prop $node "metadata"]
		set checkMetadata [isNotNull $metadata]
		if {$checkMetadata} {
			$metadata remove "lastUpdatedOn"
			$metadata remove "createdOn"
			$wordMap putAll $metadata
		}
		$wordMap put "identifier" [java::prop $node "identifier"]
		set outRelations [procGetOutRelations $node]
		set hasOutRelations [isNotEmpty $outRelations]
		if {$hasOutRelations} {
			java::for {Relation relation} $outRelations {
				set wordMap [procGetRelatedSynSetId $relation $wordMap]
			}
		}
	}
	return $wordMap
}

set result_map [java::new HashMap]
set word_list [java::new ArrayList]
java::for {String synsetId} $synset_ids {
	java::try {
		set get_node_response [getDataNode $language_id $synsetId]
		set check_error [check_response_error $get_node_response]
		if {!$check_error} {
			set graph_node [get_resp_value $get_node_response "node"]
			set wordMetadata [procGetSynsetMetadata $graph_node]
			$word_list add $wordMetadata
		}
	} catch {Exception err} {
    	puts [$err getMessage]
	}
}
$result_map put "synsets" $word_list
set response_list [create_response $result_map]
set response_csv [convert_response_to_csv $response_list "synsets"]
return $response_csv
