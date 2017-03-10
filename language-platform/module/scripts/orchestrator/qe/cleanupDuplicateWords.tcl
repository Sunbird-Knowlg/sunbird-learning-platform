package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation

proc isNotEmpty {graph_nodes} {
	set exist false
	set hasRelations [java::isnull $graph_nodes]
	if {$hasRelations == 0} {
		set relationsSize [$graph_nodes size] 
		if {$relationsSize > 0} {
			set exist true
		}
	}
	return $exist
}

proc getUntrimmedWords { position } {

	set filters [java::new HashMap]
	$filters put "objectType" "Word"
	set status [java::new ArrayList]
	#$status add "Draft"
	$filters put "status" $status
	set lemma [java::new HashMap]
	#$lemma put "endsWith" " "
	$lemma put $position [java::new String " "]
	$filters put "lemma" $lemma
	set limit [java::new Integer 10000]

	set null_var [java::null]
	set empty_list [java::new ArrayList]
	set empty_map [java::new HashMap]


	set searchResponse [indexSearch $null_var $null_var $filters $empty_list $empty_list $empty_map $empty_list $null_var $limit]
	set searchResultsMap [$searchResponse getResult]
	set wordsList [java::cast List [$searchResultsMap get "results"]]
	set wordsListNull [java::isnull $wordsList]

	set result [java::new HashMap]
	if {$wordsListNull == 0 && [$wordsList size] >= 0} {
		java::for {Object wordObj} $wordsList {
			set wordObject [java::cast Map $wordObj]		
			set identifier [$wordObject get "identifier"]
			set lemma [$wordObject get "lemma"]
			set trimmedLemma [java::cast String $lemma]
			set trimmedLemma [$trimmedLemma trim]

			$result put $trimmedLemma $wordObject
		}	
	}

	return $result

}

proc getWordsByLemmas { lemmas } {

	set filters [java::new HashMap]
	$filters put "objectType" "Word"
	set status [java::new ArrayList]
	$filters put "lemma" $lemmas
	set limit [java::new Integer 10000]

	set null_var [java::null]
	set empty_list [java::new ArrayList]
	set empty_map [java::new HashMap]

	set searchResponse [indexSearch $null_var $null_var $filters $empty_list $empty_list $empty_map $empty_list $null_var $limit]
	set searchResultsMap [$searchResponse getResult]
	set wordsList [java::cast List [$searchResultsMap get "results"]]
	set wordsListNull [java::isnull $wordsList]

	set result [java::new HashMap]
	if {$wordsListNull == 0 && [$wordsList size] >= 0} {
		java::for {Object wordObj} $wordsList {
			set wordObject [java::cast Map $wordObj]
			set identifier [$wordObject get "identifier"]
			set graph_id [$wordObject get "graph_id"]
			set ids [$result get $graph_id]
			set idsNull [java::isnull $ids]
			if {$idsNull==1} {
				set ids [java::new ArrayList]
			}
			set ids [java::cast List $ids]
			$ids add $identifier
			$result put $graph_id $ids
		}	
	}
	return $result
}


proc merge {graph_id wordNode dupWordNode} {

	set wordMetadata [java::prop $wordNode "metadata"]
	set dupWordMetadata [java::prop $dupWordNode "metadata"]

	set word_id [java::prop $wordNode "identifier"]
	set metadataUpdated false
	java::for {Map.Entry entry} [$dupWordMetadata entrySet] {
		set key [[$entry getKey] toString]
	    set value [java::cast Object [$entry getValue]]

	    set actualWordHasValue [java::isnull [$wordMetadata get $key]]
	    if {$actualWordHasValue ==1} {
	    	$wordMetadata put $key $value	
	    	set metadataUpdated true
	    }
	}
	
	if {$metadataUpdated} {
		set update_response [updateDataNode $graph_id $word_id $wordNode]
		set check_update_error [check_response_error $update_response]
		if {$check_update_error} {
			return $update_response;
		}
	}

	set dupWordInRelations [java::prop $dupWordNode "inRelations"]
	set hasRelations [isNotEmpty $dupWordInRelations]
	if {$hasRelations} {
			java::for {Relation relation} $dupWordInRelations {
				set relationType [java::prop $relation "relationType"]
				set startNodeMetadata [java::prop $relation "startNodeMetadata"]
				set startNodeFuncObjectType [$startNodeMetadata get "IL_FUNC_OBJECT_TYPE"]
				if {($relationType == "hasMember") && ([$startNodeFuncObjectType toString] == "WordSet")} {
					#Do Nothing
				} else {
					set startNodeId [java::prop $relation "startNodeId"]
					set addRelation_response [addRelation $graph_id $startNodeId $relationType $word_id]
					set check_addRelation_error [check_response_error $addRelation_response]
					if {$check_addRelation_error} {
						return $addRelation_response;
					}

				}
			}
	}

	set dupWordOutRelations [java::prop $dupWordNode "outRelations"]
	set hasRelations [isNotEmpty $dupWordOutRelations]
	if {$hasRelations} {
			java::for {Relation relation} $dupWordOutRelations {
				set relationType [java::prop $relation "relationType"]
				set endNodeId [java::prop $relation "endNodeId"]
				set addRelation_response [addRelation $graph_id $word_id $relationType $endNodeId]
				set check_addRelation_error [check_response_error $addRelation_response]
				if {$check_addRelation_error} {
					return $addRelation_response;
				}

			}
	}

	return [java::null]
}

proc cleanUp {actualWords duplicateWords} {

	java::for {Map.Entry e} [$actualWords entrySet] {
		    set graph_id [[$e getKey] toString]
		    set wordIds [java::cast ArrayList [$e getValue]]
			set wordsResp [getDataNodes $graph_id $wordIds]
			set words [get_resp_value $wordsResp "node_list"]
			set nodesExists [isNotEmpty $words]
			if {$nodesExists} {
				java::for {Node word} $words {
					set metadata [java::prop $word "metadata"]
					set lemma [$metadata get "lemma"]
					set duplicateWord [$duplicateWords get $lemma]
					set duplicateWord [java::cast Map $duplicateWord]
					set duplicateWordID [$duplicateWord get "identifier"]
					set dupWordRespone [getDataNode $graph_id [$duplicateWordID toString]]
					set check_error [check_response_error $dupWordRespone]
					if {$check_error} {
						return $dupWordRespone
					} else {
						set duplicateWordNode [get_resp_value $dupWordRespone "node"]
						set merge_resp [merge $graph_id $word $duplicateWordNode]

						if {[java::isnull $merge_resp]==0} {
							return $merge_resp
						}

						set delete_response [deleteDataNode $graph_id $duplicateWordID]
						set check_delete_error [check_response_error $delete_response]
						if {$check_delete_error} {
							return $delete_response;
						}
					}
				}
			}
	}

	return [java::null]
}

#cleaning up leading space
set leadingSpaceWords [getUntrimmedWords "startsWith"] 
set leadingSpaceWordLemmaSet [$leadingSpaceWords keySet]
set leadingSpaceWordLemmaList [java::new ArrayList $leadingSpaceWordLemmaSet]

set leadingSpaceWordsSize [$leadingSpaceWordLemmaList size]

if {$leadingSpaceWordsSize>=0} {
	set actualWords [getWordsByLemmas $leadingSpaceWordLemmaList]
	set clean_resp [cleanUp $actualWords $leadingSpaceWords]
	if {[java::isnull $clean_resp]==0} {
		return $clean_resp
	}
}

#cleaning up trailing space
set trailingSpaceWords [getUntrimmedWords "endsWith"] 
set trailingSpaceWordLemmaSet [$trailingSpaceWords keySet]
set trailingSpaceWordLemmaList [java::new ArrayList $trailingSpaceWordLemmaSet]

set trailingSpaceWordsSize [$trailingSpaceWordLemmaList size]

if {$trailingSpaceWordsSize>=0} {
	set actualWords [getWordsByLemmas $trailingSpaceWordLemmaList]
	set clean_resp [cleanUp $actualWords $trailingSpaceWords]
	if {[java::isnull $clean_resp]==0} {
		return $clean_resp
	}

}

set result_map [java::new HashMap]
$result_map put "status" "OK"
set response_list [create_response $result_map]
return $response_list

