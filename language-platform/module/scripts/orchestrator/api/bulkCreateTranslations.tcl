package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation
java::import -package com.ilimi.common.dto Response

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

proc getWord { graph_id  lemma } {

	set filters [java::new HashMap]
	$filters put "objectType" "Word"
	$filters put "graph_id" $graph_id
	set status [java::new ArrayList]
	$filters put "status" $status
	$filters put "lemma" $lemma
	set limit [java::new Integer 1]

	set searchCriteria [java::new HashMap]
	$searchCriteria put "filters" $filters
	$searchCriteria put "limit" $limit

	set searchResponse [compositeSearch $searchCriteria]
	set searchResultsMap [$searchResponse getResult]
	set wordsList [java::cast List [$searchResultsMap get "results"]]
	set wordsListNull [java::isnull $wordsList]

	if {$wordsListNull == 0 && [$wordsList size] == 1} {
		
		set wordObj [$wordsList get 0]
		set wordObject [java::cast Map $wordObj]
		return  $wordObject
	}
	return [java::null]
}

proc getInNodeRelationIds {graph_node relationType relationName property} {

	set relationIds [java::new ArrayList]
	set inRelations [java::prop $graph_node "inRelations"]
	set hasRelations [isNotEmpty $inRelations]
	if {$hasRelations} {
		java::for {Relation relation} $inRelations {
			if {[java::prop $relation "startNodeObjectType"] == $relationType && [java::prop $relation "relationType"] == $relationName} {
				set prop_value [java::prop $relation $property]
				$relationIds add $prop_value
			}
		}
	}
	return $relationIds
}

proc getSynonym {language_id word_node} {
	set word_metadata [java::prop $word_node "metadata"]
	set word_metadata [java::cast Map $word_metadata]
	set word_id [java::prop $word_node "identifier"]
	set lemma [$word_metadata get "lemma"]
	set pmId [$word_metadata get "primaryMeaningId"]
	#puts "word_id is $word_id | pmId is $pmId"
	set isPrimaryIdNull [java::isnull $pmId]
	#if primary meaning is null
	if {$isPrimaryIdNull == 1} {
		#get words synonym list
		set synonym_list [getInNodeRelationIds $word_node "Synset" "synonym" "startNodeId"]
		set synonym_list [java::cast List $synonym_list]
		
		set not_empty_list [isNotEmpty $synonym_list]
		
		if {$not_empty_list} {
			#return first synset id if word has some synonyms
			set pmId [$synonym_list get 0]
			set pmId [java::cast String $pmId]
			
		} else {
			#create synset as word
			set synsetResponse [createSynsetNode $language_id $lemma]
			set check_error [check_response_error $synsetResponse]
			if {$check_error} {
				#puts "error while creating synset $language_id $lemma"
				return $synsetResponse
			}
			set pmId [get_resp_value $synsetResponse "node_id"]
			set pmId [java::cast String $pmId]
		}

		#add synsetid as primary meaning in word
		set addPrimaryMeaningResp [addPrimaryMeaning $language_id $word_id $pmId true]
		set addPrimaryMeaningRespNull [java::isnull $addPrimaryMeaningResp]
		if {$addPrimaryMeaningRespNull == 0} {
			return $addPrimaryMeaningResp
		}

	}

	return $pmId
}

proc createSynsetNode {language_id gloss} {
	set synsetMetaData [java::new HashMap]
	$synsetMetaData put "gloss" $gloss
	set response [createSynset $language_id $synsetMetaData]
	return $response
}

proc createWordNode {language_id lemma} {

	set synsetResponse [createSynsetNode $language_id $lemma]
	set check_error [check_response_error $synsetResponse]
	if {$check_error} {
		#puts "error while creating synset $language_id $lemma"
		return $synsetResponse
	} 
	set nodePrimaryMeaningId [get_resp_value $synsetResponse "node_id"]
	set nodePrimaryMeaningId [$nodePrimaryMeaningId toString]

	#puts "createWordNode | nodePrimaryMeaningId $nodePrimaryMeaningId"
	set graph_node [java::new Node [java::null] "DATA_NODE" "Word"]
	set metadata [java::new HashMap]
	set trimmedLemma [string trim $lemma]
	#puts "createWordNode | trimmedLemma $trimmedLemma"

	$metadata put "lemma" $trimmedLemma
	$metadata put "primaryMeaningId" $nodePrimaryMeaningId
	$graph_node setMetadata $metadata
	#puts "creating word - [$metadata toString]"
	set create_response [createDataNode $language_id $graph_node]
	set check_error [check_response_error $create_response]
	if {$check_error} {
		#puts "error while creating word $language_id [$metadata toString]"
		return $wordResponse
	}

	set wordId [get_resp_value $create_response "node_id"]
	set wordId [$wordId toString]

	$graph_node setIdentifier $wordId

	set addPrimaryMeaningResp [addPrimaryMeaning $language_id $wordId $nodePrimaryMeaningId false]
	set addPrimaryMeaningRespNull [java::isnull $addPrimaryMeaningResp]
	if {$addPrimaryMeaningRespNull == 0} {
		return $addPrimaryMeaningResp
	}
	return $graph_node
}

proc addPrimaryMeaning {language_id wordId synsetId update} {

	#puts "addPrimaryMeaning $language_id $wordId $synsetId $update"
	if {$update} {
		#puts "updating node with primaryMeaningId $synsetId"
		set graph_node [java::new Node $wordId "DATA_NODE" "Word"]
		set metadata [java::new HashMap]
		$metadata put "primaryMeaningId" $synsetId
		$graph_node setMetadata $metadata
		#puts "update word $wordId metadata [$metadata toString]"
		set wordResponse [updateDataNode $language_id $wordId $graph_node]
		set check_error [check_response_error $wordResponse]
		if {$check_error} {
			#puts "error while updating word with primary meaning id"
			return $wordResponse
		}
	}

	set addRelation_response [addRelation $language_id $synsetId "synonym" $wordId]
	set check_addRelation_error [check_response_error $addRelation_response]
	if {$check_addRelation_error} {
		#puts "error while adding synonym relation between word and synset"
		return $addRelation_response;
	}

	set words [java::new ArrayList]
	$words add $wordId
	set enrich [enrichWords $language_id $words]

	return [java::null]
}


proc createTranslations {wordMap} {

	set maindWordLanguageId ""
	set mainWord ""
	set mainSynset ""
	set translationSetMap [java::new HashMap]
	set errors [java::new ArrayList]
	set mainVariablePopulated false
	java::for {Map.Entry entry} [$wordMap entrySet] {
		set language_id [[$entry getKey] toString]
	    set word_lemma [[$entry getValue] toString]
	    set word_lemma [string trim $word_lemma]
	    if {$word_lemma == ""} {
	    	continue
	    }
	   	#puts "language_id $language_id word_lemma $word_lemma"
	   	set word [getWord $language_id $word_lemma]

   		set wordNull [java::isnull $word]
		set word_node [java::null]
		if {!$wordNull} {
   			set wordId [$word get "identifier"]
   			set wordId [$wordId toString]
   			#puts "wordId $wordId $language_id"
			set get_node_response [getDataNode $language_id $wordId]
			set check_error [check_response_error $get_node_response]

			if {$check_error} {
				set wordNull true
			} else {
				set word_node [get_resp_value $get_node_response "node"]
			}	

		}

   		#puts "wordNull $wordNull"
   		if {$wordNull} {
   			#puts "word not found"
   			set word_node [createWordNode $language_id  $word_lemma]
   			set wordId ""
   			set synsetId ""
   			
   			set resp_instance [java::instanceof $word_node Response]
			if {$resp_instance == 0} {
				set wordId [java::prop $word_node "identifier"]
				set word_metadata [java::prop $word_node "metadata"]
				set synsetId [[$word_metadata get "primaryMeaningId"] toString]
			} else {
				set errorMsgMap [java::prop $word_node "result"]
				$errors add "error while creating the word $word_lemma, language_id $language_id, error  [$errorMsgMap toString]"
				continue
			}

	   	} else {
   			#puts "word found - [$word toString]"
			set synsetId [getSynonym $language_id $word_node]

			set resp_instance [java::instanceof $synsetId Response]
			if {$resp_instance == 0} {
				set synsetId [$synsetId toString]
			} else {
				set errorMsgMap [java::prop $synsetId "result"]
				$errors add "error while getting synonym for the word $word_lemma, language_id $language_id, error  [$errorMsgMap toString]"
				continue
			}
	   	}

	   	if {$mainVariablePopulated} {
	   		#puts "populating $language_id $synsetId into translationSetMap"
	   		set synsets [java::new ArrayList]
	   		$synsets add $synsetId
	   		$translationSetMap put $language_id $synsets
	   	} else {
	   		set mainVariablePopulated true
	   		#puts "entering main info $language_id $wordId $synsetId "
	   		set mainWord $wordId
	   		set mainSynset $synsetId
	   		set maindWordLanguageId $language_id
	   	} 

	}

	#puts "maindWordLanguageId $maindWordLanguageId  mainWord $mainWord mainSynset $mainSynset translationSetMap [$translationSetMap toString]"
	if {[$translationSetMap size] > 0} {
		set translationMap [java::new HashMap]
		$translationMap put $mainSynset $translationSetMap
		set createTranslationResponse [createTranslationAsync $maindWordLanguageId $mainWord $translationMap]
		set check_error [check_response_error $createTranslationResponse]
		if {$check_error} {
				set errorMsgMap [java::prop $createTranslationResponse "result"]
				set errorResponseParams [[java::prop $createTranslationResponse "params"] toString]
				$errors add " create tranlsation failure input  maindWordLanguageId $maindWordLanguageId  mainWord $mainWord translationMap [$translationMap toString], error [$errorMsgMap toString], errorResponseParams $errorResponseParams"
		}
	} else {
		$errors add " create tranlsation failure input  maindWordLanguageId $maindWordLanguageId  mainWord $mainWord translationSetMap [$translationSetMap toString], error is no proper synset"		
	}

	#puts "errors [$errors toString]"
	return $errors
}


#starts
set errors [java::new ArrayList]
set result_map [java::new HashMap]

java::for {Map word} $words {
		set wordMap [$word get "word"]
		set wordMap [java::cast Map $wordMap]
		set resp [createTranslations $wordMap]
		$errors addAll $resp
}

$result_map put "errors" $errors
set response_list [create_response $result_map]
return $response_list