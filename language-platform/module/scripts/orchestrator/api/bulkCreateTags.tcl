package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation
java::import -package com.ilimi.common.dto Response
java::import -package java.util Arrays

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

proc getSynonym {language_id word_node tags} {
	set word_metadata [java::prop $word_node "metadata"]
	set word_metadata [java::cast Map $word_metadata]
	set word_id [java::prop $word_node "identifier"]
	set lemma [$word_metadata get "lemma"]
	set pmId [$word_metadata get "primaryMeaningId"]
	#puts "word_id is $word_id | pmId is $pmId | tags is [$tags toString]"
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
			
			set synsetResponse [updateSynsetNode $language_id $pmId $tags]
			set check_error [check_response_error $synsetResponse]
			if {$check_error} {
				#puts "error while updating synset $language_id $pmId"
				return $synsetResponse
			}

		} else {
			#create synset as word
			set synsetResponse [createSynsetNode $language_id $lemma $tags]
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

	} else {
			set synsetResponse [updateSynsetNode $language_id $pmId $tags]
			set check_error [check_response_error $synsetResponse]
			if {$check_error} {
				#puts "error while updating synset $language_id $pmId"
				return $synsetResponse
			}

			set enrich [enrichWordAsync $language_id $word_id]
	}

	#puts "primaryMeaningId [$pmId toString]"
	return $pmId
}


proc enrichWordAsync {language_id wordId} {
	set words [java::new ArrayList]
	$words add $wordId
	set enrich [enrichWords $language_id $words]

}

proc updateSynsetNode {language_id  synset_id tags} {
	set synsetMetaData [java::new HashMap]
	$synsetMetaData put "tags" $tags
	#puts "updating synset $language_id $synset_id metadata [$synsetMetaData toString]"
	set response [updateSynset $language_id $synset_id $synsetMetaData]
	return $response
}

proc createSynsetNode {language_id gloss tags} {
	set synsetMetaData [java::new HashMap]
	$synsetMetaData put "gloss" $gloss
	$synsetMetaData put "tags" $tags
	#puts "creating synset $language_id metadata [$synsetMetaData toString]"
	set response [createSynset $language_id $synsetMetaData]
	return $response
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

	set enrich [enrichWordAsync $language_id $wordId]

	return [java::null]
}


proc createTag {wordMap tags} {

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
		set wordId ""

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
			$errors add "error the word not found $word_lemma, language_id $language_id"
			continue

	   	} else {
   			#puts "word found - [$word toString]"
			set synsetId [getSynonym $language_id $word_node $tags]

			set resp_instance [java::instanceof $synsetId Response]
			if {$resp_instance == 0} {
				set synsetId [$synsetId toString]
			} else {
				set errorMsgMap [java::prop $synsetId "result"]
				$errors add "error while getting synonym for the word $word_lemma, language_id $language_id, error  [$errorMsgMap toString]"
				continue
			}


	   	}


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
		set tags [$word get "tags"]
		set tags [java::cast String $tags]
		set tagList [java::call Arrays asList [$tags split "\\s*,\\s*"]]
		set tagList [java::new ArrayList $tagList]
		#puts "tagList [$tagList toString]"
		#$errors add [$tagList toString]
		set resp [createTag $wordMap $tagList]
		$errors addAll $resp
}

$result_map put "errors" $errors
set response_list [create_response $result_map]
return $response_list