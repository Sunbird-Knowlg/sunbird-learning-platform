package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation

set object_type "Word"

proc getDefinitionFromCache {defMap language_id object_type} {

	set definition [$defMap get $language_id]
	set definitionNull [java::isnull $definition]
	if {$definitionNull == 1} {
		set resp_def_node [getDefinition $language_id $object_type]
		set def_node [get_resp_value $resp_def_node "definition_node"]
		$defMap put $language_id $def_node
		set definition $def_node
	}
	return $definition
}

proc getWordsByProp {exist} {
	set filters [java::new HashMap]
	$filters put "objectType" "Word"
	set status [java::new ArrayList]
	$status add "Draft"
	$filters put "status" $status
	set limit [java::new Integer 10000]
	set exists [java::new ArrayList]
	$exists add $exist
	#$exists add "thresholdLevel"
	#$exists add "grade"


	set null_var [java::null]
	set empty_list [java::new ArrayList]
	set empty_map [java::new HashMap]

	set searchResponse [indexSearch $null_var $null_var $filters $exists $empty_list $empty_map $empty_list $null_var $limit]
	set searchResultsMap [$searchResponse getResult]
	set wordsList [java::cast List [$searchResultsMap get "results"]]

	return $wordsList
}

proc makeAliveWordsHaving { existProperty } {

	set object_type "Word"
	set wordsList [getWordsByProp $existProperty]
	set wordsListNull [java::isnull $wordsList]

	if {$wordsListNull == 0 && [$wordsList size] >= 0} {
		set defMap [java::new HashMap]
		java::for {Object wordObj} $wordsList {
			set wordObject [java::cast Map $wordObj]		
			set identifier [$wordObject get "identifier"]
			set language_id [$wordObject get "graph_id"]
			set word [java::new HashMap]
			$word put "identifier" $identifier
			$word put "objectType" $object_type
			$word put "status" "Live"
			set def_node [getDefinitionFromCache $defMap $language_id $object_type]
			set word_obj [convert_to_graph_node $word $def_node]
			set update_response [updateDataNode $language_id $identifier $word_obj]
			set check_error [check_response_error $update_response]
			if {$check_error} {
				return $update_response;
			}
		}
	}

	return [java::null]

}


set result [makeAliveWordsHaving "thresholdLevel"]
set resultNull [java::isnull $result]

if {$resultNull == 0} {
	#return error response
	return $result
}


set result [makeAliveWordsHaving "grade"]
set resultNull [java::isnull $result]

if {$resultNull == 0} {
	#return error response
	return $result
}


set result_map [java::new HashMap]
$result_map put "status" "OK"
set response_list [create_response $result_map]
return $response_list


