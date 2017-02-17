package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation

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

proc getWordList {wordIds language_id} {
	set filters [java::new HashMap]
	$filters put "objectType" "Word"
	$filters put "graph_id" $language_id
	$filters put "identifier" $wordIds
	$filters put "status" [java::new ArrayList]
	set limit [java::new Integer 10000]

	set null_var [java::null]
	set empty_list [java::new ArrayList]
	set empty_map [java::new HashMap]

	set searchResponse [indexSearch $null_var $null_var $filters $empty_list $empty_list $empty_map $empty_list $null_var $limit]
	set searchResultsMap [$searchResponse getResult]
	set wordsList [java::cast List [$searchResultsMap get "results"]]
	set wordsListNull [java::isnull $wordsList]

	set result [java::new ArrayList]
	if {$wordsListNull == 0 && [$wordsList size] >= 0} {
		java::for {Object wordObj} $wordsList {
			set wordObject [java::cast Map $wordObj]		
			set identifier [$wordObject get "identifier"]
			set lemma [$wordObject get "lemma"]
			set word [java::new HashMap]
			$word put "lemma" $lemma
			$word put "identifier" $identifier
			$result add $word
		}	
	}
	return $result
}

proc getRhymingSoundWords {word_node language_id} {
	set relationIds [java::new ArrayList]
	set inRelations [java::prop $word_node "inRelations"]
	set actualWordId [java::prop $word_node "identifier"]
	set hasRelations [isNotEmpty $inRelations]
	if {$hasRelations} {
		java::for {Relation relation} $inRelations {
			set startType [java::prop $relation "startNodeObjectType"]
			if {[java::prop $relation "startNodeObjectType"] == "WordSet"} {
				set start_node_metadata [java::prop $relation "startNodeMetadata"]
				set hasMetadata [isNotEmpty $start_node_metadata]
				if {$hasMetadata} {
					set wordSetType [$start_node_metadata get "type"]
					set wordSetType [$wordSetType toString]
					if { $wordSetType == "RhymingSound"} {
						set setId [java::prop $relation "startNodeId"]
						set rhymingSoundResponse [getSetMembers $language_id $setId]
						set rhymingSoundSetMembers [get_resp_value $rhymingSoundResponse "members"]
						set hasRhymingSoundSetMembers [isNotEmpty $rhymingSoundSetMembers]
						if {$hasRhymingSoundSetMembers} {
							$rhymingSoundSetMembers remove $actualWordId
							set hasRhymingSoundSetMembers [isNotEmpty $rhymingSoundSetMembers]
							if {$hasRhymingSoundSetMembers} {
								set rhymingSoundWords [getWordList $rhymingSoundSetMembers $language_id]
								return $rhymingSoundWords
							}
						}
					}
				}
			}
		}
	}
	return [java::new ArrayList]
}

set searchProperty [java::new HashMap]
$searchProperty put "lemma" $lemma
set property [create_search_property $searchProperty]
set search_response [getNodesByProperty $language_id $property]

set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} 

set graph_nodes [get_resp_value $search_response "node_list"]
set isListNull [java::isnull $graph_nodes]
if {$isListNull == 0} {
	#set graph_nodes [java::cast ArrayList $graph_nodes]
	set listSize [$graph_nodes size]
	if {$listSize > 0} {
		set word_node [$graph_nodes get 0]
		set word_node [java::cast Node $word_node]
		set words [getRhymingSoundWords $word_node $language_id]
		# create rhyming sound words response
		set resp_object [java::new HashMap]
		$resp_object put "words" $words
		set response [create_response $resp_object]
		return $response
	}
}



