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

proc filterSynset {synset_ids languages} {

	set filteredSynsets [java::new ArrayList]

	java::for {String synset_id} $synset_ids {
		set index 0
		set invalidSynset 0
		set idArray [split $synset_id ":"]
		foreach entry $idArray {
			set languageContains [$languages contains $entry]
			if {$languageContains == 1} {
				$filteredSynsets add $synset_id
		 	}
			set index [expr $index + 1]
		}
		if {$index == 1} {
			set idArray [split $synset_id "_"]
			foreach entry $idArray {
				set languageContains [$languages contains $entry]
				if {$languageContains == 1} {
					$filteredSynsets add $synset_id
		 		}
			 	set invalidSynset [expr $invalidSynset + 1]	
		 	}
		}
		if {$invalidSynset == 1} {
			set languageContains [$languages contains "en"]
			if {$languageContains == 1} {
				$filteredSynsets add $synset_id
	 		}
		}
	}

	return $filteredSynsets
}

proc getInNodeRelationIds {graph_node relationType relationName property} {

	set relationIds [java::new ArrayList]
	set inRelations [getInRelations $graph_node]
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

set filters [java::new HashMap]
$filters put "objectType" "Word"
$filters put "graph_id" $language_id
$filters put "lemma" $lemma
$filters put "status" [java::new ArrayList]
set limit [java::new Integer 1]

set indexSearchCriteria [java::new HashMap]
$indexSearchCriteria put "filters" $filters
$indexSearchCriteria put "limit" $limit

set searchResponse [compositeSearch $indexSearchCriteria]
set searchResultsMap [$searchResponse getResult]
set wordsList [java::cast List [$searchResultsMap get "results"]]
set wordsListNull [java::isnull $wordsList]
if {$wordsListNull == 1 || [$wordsList size] == 0} {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_WORD_NOT_FOUND"
	$result_map put "message" "Word not found"
	$result_map put "responseCode" [java::new Integer 404]
	set response_list [create_error_response $result_map]
	return $response_list
}

set wordObject [java::cast Map [$wordsList get 0]]
set word_id [$wordObject get "identifier"]
set object_type "TranslationSet"
set graph_id "translations"
set node_id $word_id
set get_node_response [getDataNode $language_id $node_id]
set get_node_response_error [check_response_error $get_node_response]
if {$get_node_response_error} {
	return $get_node_response
}


set word_node [get_resp_value $get_node_response "node"]
set synonym_list [getInNodeRelationIds $word_node "Synset" "synonym" "startNodeId"]
set synset_list [java::new ArrayList]
$synset_list addAll $synonym_list

set not_empty_list [isNotEmpty $synset_list]
if {$not_empty_list == false} {
	set result_map [java::new HashMap]
	set response_list [create_response $result_map]
	return $response_list
}

set filters [java::new HashMap]
$filters put "objectType" $object_type
$filters put "graph_id" $graph_id
$filters put "synsets" $synset_list
$filters put "status" [java::new ArrayList]

set indexSearchCriteria [java::new HashMap]
$indexSearchCriteria put "filters" $filters
set searchResponse [compositeSearch $indexSearchCriteria]
set searchResultsMap [$searchResponse getResult]
set translations [java::cast List [$searchResultsMap get "results"]]
set translationsNull [java::isnull $translations]
if {$translationsNull == 1 || [$translations size] == 0} {
	set result_map [java::new HashMap]
	set response_list [create_response $result_map]
	return $response_list
}

set result_map [java::new HashMap]
set result_list [java::new HashMap]

java::try {
	java::for {Object translation} $translations {
		set translation [java::cast Map $translation]
		set synsets [$translation get "synsets"]
		set current_language [java::new ArrayList]
		$current_language add $language_id
		set synset_id_list [java::new ArrayList]
		set current_synset_id [filterSynset $synsets $current_language]
		set synsetObjectResponse [multiLanguageWordSearch $current_synset_id]
		set synsetMap [java::cast Map [$synsetObjectResponse get "translations"]]
		set synsetId [[[$synsetMap keySet] iterator] next]
		set synsetObjectMap [java::cast Map [$synsetMap get $synsetId]]
		$synsetObjectMap remove $language_id

		set synset_ids [filterSynset $synsets $languages]
		set not_empty_list [isNotEmpty $synset_ids]
		if {$not_empty_list} {
			$synset_id_list addAll $synset_ids
			set searchResponse [multiLanguageWordSearch $synset_id_list]
			set searchResultsMap [java::cast Map [$searchResponse get "translations"]]
			set mapValues [$searchResultsMap values]
			java::for {Object obj} $mapValues {
				set mapValue [java::cast Map $obj]
				$mapValue remove "gloss"
				$synsetObjectMap putAll $mapValue
			}
		}
		$result_list putAll $synsetMap
		
	}
	$result_map put "translations" $result_list
} catch {Exception err} {
	$result_map put "error" [$err getMessage]
}
set response_list [create_response $result_map]
return $response_list
