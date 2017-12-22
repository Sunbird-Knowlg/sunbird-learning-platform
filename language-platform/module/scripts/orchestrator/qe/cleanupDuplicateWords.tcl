package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node Relation

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

proc getUntrimmedWords { position graph_id } {

	set filters [java::new HashMap]
	$filters put "objectType" "Word"
	$filters put "graph_id" $graph_id
	set status [java::new ArrayList]
	#$status add "Draft"
	$filters put "status" $status
	set lemma [java::new HashMap]
	#$lemma put "endsWith" " "
	$lemma put $position [java::new String " "]
	$filters put "lemma" $lemma
	set limit [java::new Integer 10000]

	set searchCriteria [java::new HashMap]
	$searchCriteria put "filters" $filters
	$searchCriteria put "limit" $limit

		
	#puts "cleanupDuplicateWords | searchCriteria  [$searchCriteria toString]"
	set searchResponse [compositeSearch $searchCriteria]
	set searchResultsMap [$searchResponse getResult]
	#puts "cleanupDuplicateWords | searchResultsMap size [$searchResultsMap size]"
	set wordsList [java::cast List [$searchResultsMap get "results"]]
	set wordsListNull [java::isnull $wordsList]
	set result [java::new HashMap]

	if {$wordsListNull == 0 && [$wordsList size] >= 0} {
		#puts "cleanupDuplicateWords | wordsList size [$wordsList size]"
		java::for {Object wordObj} $wordsList {
			set wordObject [java::cast Map $wordObj]		
			set lemma [$wordObject get "lemma"]
			set trimmedLemma [java::cast String $lemma]
			set trimmedLemma [$trimmedLemma trim]

			$result put $trimmedLemma $wordObject
		}	

	}

	return $result

}

proc getWordsByLemmas { lemmas graph_id } {

	set filters [java::new HashMap]
	$filters put "objectType" "Word"
	$filters put "graph_id" $graph_id
	set status [java::new ArrayList]
	$filters put "status" $status
	$filters put "lemma" $lemmas
	set limit [java::new Integer 10000]

	#puts "cleanupDuplicateWords | lemmas size [$lemmas size]"

	set searchCriteria [java::new HashMap]
	$searchCriteria put "filters" $filters
	$searchCriteria put "limit" $limit

	#puts "cleanupDuplicateWords | getWordsByLemmas-searchCriteria  [$searchCriteria toString]"
	set searchResponse [compositeSearch $searchCriteria]
	set searchResultsMap [$searchResponse getResult]
	set wordsList [java::cast List [$searchResultsMap get "results"]]
	set wordsListNull [java::isnull $wordsList]
	#puts "cleanupDuplicateWords | wordsListNull $wordsListNull"

	set result [java::new HashMap]
	if {$wordsListNull == 0 && [$wordsList size] >= 0} {
		#puts "cleanupDuplicateWords | wordsList [$wordsList size]"
		java::for {Object wordObj} $wordsList {
			set wordObject [java::cast Map $wordObj]
			set identifier [$wordObject get "identifier"]
			set graph_id [$wordObject get "graph_id"]
			set lemma [$wordObject get "lemma"]
			set ids [$result get $graph_id]
			set idsNull [java::isnull $ids]
			if {$idsNull==1} {
				set ids [java::new ArrayList]
			}
			set ids [java::cast List $ids]
			$ids add $identifier
			$result put $graph_id $ids
			$lemmas remove $lemma
		}
	}
	#puts "cleanupDuplicateWords | getWordsByLemmas done"
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
		set node_update [java::new Node $word_id "DATA_NODE" "Word"]
		$node_update setMetadata $wordMetadata
		#puts "cleanupDuplicateWords | update node metadata = [[$node_update getMetadata] toString]"
		set update_response [updateDataNode $graph_id $word_id $node_update]
		set check_update_error [check_response_error $update_response]
		if {$check_update_error} {
			return $update_response;
		}
	}

	#puts "cleanupDuplicateWords | update node inRelations $word_id"

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

	#puts "cleanupDuplicateWords | update node outRelations $word_id"
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
			#puts "cleanupDuplicateWords | graph_id $graph_id wordIds size [$wordIds size]"
			#puts "cleanupDuplicateWords | wordIds [$wordIds toString]"
			set wordsResp [getDataNodes $graph_id $wordIds]
			set words [get_resp_value $wordsResp "node_list"]
			set nodesExists [isNotEmpty $words]
			if {$nodesExists} {
				#puts "cleanupDuplicateWords | words size [$words size]"
				java::for {Node word} $words {
					set metadata [java::prop $word "metadata"]
					set id [java::prop $word "identifier"]
					#puts "cleanupDuplicateWords | word id $id- metadata [$metadata toString]"
					set lemma [$metadata get "lemma"]
					set duplicateWord [$duplicateWords get $lemma]
					set duplicateWord [java::cast Map $duplicateWord]
					set duplicateWordID [$duplicateWord get "identifier"]
					set duplicateWordID [$duplicateWordID toString]
					#puts "cleanupDuplicateWords | getting Node - duplicateWordID - $duplicateWordID"
					set dupWordRespone [getDataNode $graph_id $duplicateWordID]
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

proc trim {wordsNotFound duplicateWords} {

	java::for {String word} $wordsNotFound {
		set duplicateWord [$duplicateWords get $word]
		set duplicateWord [java::cast Map $duplicateWord]
		set duplicateWordID [$duplicateWord get "identifier"]
		set duplicateWordID [$duplicateWordID toString]
		set duplicateWordlemma [$duplicateWord get "lemma"]
		set duplicateWordlemma [$duplicateWordlemma toString]
		set graph_id [$duplicateWord get "graph_id"]
		set graph_id [$graph_id toString]
		set node_update [java::new Node $duplicateWordID "DATA_NODE" "Word"]
		set wordMetadata [java::new HashMap]
		$wordMetadata put "lemma" $word
		$node_update setMetadata $wordMetadata
		#puts "cleanupDuplicateWords | update $duplicateWordID, $duplicateWordlemma node metadata = [$wordMetadata toString]"
		set update_response [updateDataNode $graph_id $duplicateWordID $node_update]
		set check_update_error [check_response_error $update_response]
		if {$check_update_error} {
			return $update_response;
		}

	}
	
	return [java::null]
}

#cleaning up leading space
set leadingSpaceWords [getUntrimmedWords "startsWith" $graph_id] 
#puts "cleanupDuplicateWords | leadingSpaceWords [$leadingSpaceWords size]"
set leadingSpaceWordLemmaSet [$leadingSpaceWords keySet]
set leadingSpaceWordLemmaList [java::new ArrayList $leadingSpaceWordLemmaSet]

set leadingSpaceWordsSize [$leadingSpaceWordLemmaList size]
#puts "cleanupDuplicateWords | leadingSpaceWordsSize $leadingSpaceWordsSize"
set start 0
while {$leadingSpaceWordsSize>0} {

	if {$leadingSpaceWordsSize > 500} {
		set end [expr {$start + 500}]
	} else {
		set end [expr {$start + $leadingSpaceWordsSize}]
	}
	#puts "cleanupDuplicateWords | start $start end $end"
	set leadingSpaceWordsSubList [$leadingSpaceWordLemmaList subList $start $end]
	#puts "cleanupDuplicateWords | leadingSpaceWordsSubList size [$leadingSpaceWordsSubList size]"
	set actualWords [getWordsByLemmas $leadingSpaceWordsSubList $graph_id]
	#puts "cleanupDuplicateWords | lemmas [$leadingSpaceWordsSubList size] after removal"	
	set trim_resp [trim $leadingSpaceWordsSubList $leadingSpaceWords]
	if {[java::isnull $trim_resp]==0} {
		set errorMsgMap [java::prop $trim_resp "result"]
		#puts "cleanupDuplicateWords | exception during startsWith trim, error [$errorMsgMap toString]"
		return $trim_resp
	}	
	set clean_resp [cleanUp $actualWords $leadingSpaceWords]
	if {[java::isnull $clean_resp]==0} {
		set errorMsgMap [java::prop $clean_resp "result"]
		puts "cleanupDuplicateWords | exception during startsWith cleanup, error [$errorMsgMap toString]"
		return $clean_resp
	}
	set start [expr {$end + 0}]
	set leadingSpaceWordsSize [expr {$leadingSpaceWordsSize - 500}]
	#puts "cleanupDuplicateWords | leadingSpaceWordsSize $leadingSpaceWordsSize"

}
	#puts "cleanupDuplicateWords | startsWith duplicateWord processed with es limit of 10000"

#cleaning up trailing space
set trailingSpaceWords [getUntrimmedWords "endsWith" $graph_id] 
#puts "cleanupDuplicateWords | trailingSpaceWords [$trailingSpaceWords  size]"
set trailingSpaceWordLemmaSet [$trailingSpaceWords keySet]
set trailingSpaceWordLemmaList [java::new ArrayList $trailingSpaceWordLemmaSet]

set trailingSpaceWordsSize [$trailingSpaceWordLemmaList size]
#puts "cleanupDuplicateWords | trailingSpaceWordsSize $trailingSpaceWordsSize"
set start 0
while {$trailingSpaceWordsSize>0} {

	if {$trailingSpaceWordsSize > 500} {
		set end [expr {$start + 500}]
	} else {
		set end [expr {$start + $trailingSpaceWordsSize}]
	}
	#puts "cleanupDuplicateWords | start $start end $end"
	set trailingSpaceWordsSubList [$trailingSpaceWordLemmaList subList $start $end]
	#puts "cleanupDuplicateWords | trailingSpaceWordsSubList size [$trailingSpaceWordsSubList size]"
	set actualWords [getWordsByLemmas $trailingSpaceWordsSubList $graph_id]
	#puts "cleanupDuplicateWords | lemmas [$trailingSpaceWordsSubList size] after removal"	
	set trim_resp [trim $trailingSpaceWordsSubList $trailingSpaceWords]
	if {[java::isnull $trim_resp]==0} {
		set errorMsgMap [java::prop $trim_resp "result"]
		#puts "cleanupDuplicateWords | exception during endsWith trim, error [$errorMsgMap toString]"
		return $trim_resp
	}	
	set clean_resp [cleanUp $actualWords $trailingSpaceWords]
	if {[java::isnull $clean_resp]==0} {
		set errorMsgMap [java::prop $clean_resp "result"]
		puts "cleanupDuplicateWords | exception during endsWith cleanup, error [$errorMsgMap toString]"
		return $clean_resp
	}
	set start [expr {$end + 0}]
	set trailingSpaceWordsSize [expr {$trailingSpaceWordsSize - 500}]
	#puts "cleanupDuplicateWords | trailingSpaceWordsSize $trailingSpaceWordsSize"

}
	#puts "cleanupDuplicateWords | endsWith duplicateWord processed with es limit of 10000"

set result_map [java::new HashMap]
$result_map put "status" "OK"
set response_list [create_response $result_map]
return $response_list

