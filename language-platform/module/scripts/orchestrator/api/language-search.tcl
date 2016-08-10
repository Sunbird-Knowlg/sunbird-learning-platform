package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set objectType [java::null]
set graphId "domain"
set wordChains_limit 0
set ruleNode [java::null]
set weightagesMap [java::new HashMap]
set baseConditions [java::new HashMap]
set fuzzySearch "false"
set wordChainsQuery "false"
set languageIdSize 0
set request_map [java::new HashMap]
set wordChainsLimit 10
set traversalRuleDefinition "TraversalRule"


set isFuzzyNull [java::isnull $fuzzy]
if {$isFuzzyNull == 0} {
	set fuzzyString [$fuzzy toString]
	set fuzzySearch $fuzzyString
}

set isTraversalIdNull [java::isnull $traversals]
if {$isTraversalIdNull == 0} {
	set fuzzySearch "true"
	set wordChainsQuery "true"
	set traversalsSize [$traversals size]
	if {$traversalsSize > 0} {
		set traversalId [$traversals get 0]
	}
}

set isLimitNull [java::isnull $limit]
if {$isLimitNull == 0} {
	set wordChainsLimit $limit
}

set languageIdObj [$filters get "language_id"]
set languageId [java::cast ArrayList $languageIdObj]

set isLanguageIdNull [java::isnull $languageId]
if {$isLanguageIdNull == 0} {
	set languageIdSize [$languageId size]
	if {$languageIdSize > 0} {
		set language [$languageId get 0]
		set graphId $language
	}
	$filters remove "language_id"
	$filters put "graph_id" $languageIdObj
}


$request_map put "query" $query
$request_map put "exists" $exists
$request_map put "not_exists" $not_exists
$request_map put "sort_by" $sort_by
$request_map put "facets" $facets
$request_map put "limit" $limit
$request_map put "fuzzy" $fuzzy

if {$wordChainsQuery == "true"} {
	$filters remove "graph_id"
	if {$isLanguageIdNull == 1 || $languageIdSize == 0} {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_CONTENT_INVALID_REQUEST"
		$result_map put "message" "At least one language Id is mandatory"
		$result_map put "responseCode" [java::new Integer 400]
		set response_list [create_error_response $result_map]
		return $response_list
	}
	
	$request_map put "traversal" [java::new Boolean "true"]
	
	set get_rule_response [getDataNode $graphId $traversalId]
	set get_rule_response_error [check_response_error $get_rule_response]
	if {$get_rule_response_error} {
		return $get_rule_response
	}
	
	set ruleNode [get_resp_value $get_rule_response "node"]
		
	set rule_def_node [getDefinition $graphId $traversalRuleDefinition]
	set def_node [get_resp_value $rule_def_node "definition_node"]

	set ruleObject [convert_graph_node $ruleNode $def_node]
	set ruleMetadata [$ruleNode getMetadata]
	
	set objectType [$ruleObject get "ruleObjectType"]
	set searchResultsLimit [$ruleObject get "wordChainWordsSize"]
	
	$request_map put "limit" $searchResultsLimit
	set fuzzyObj [java::new Boolean "true"]
	$request_map put "fuzzy" $fuzzyObj
}

if {$fuzzySearch == "true"} {
	set isObjectTypeNull [java::isnull $objectType]
	if {$isObjectTypeNull == 1} {
		set objectType [$filters get "objectType"]
	}
	
	set isObjectTypeNull [java::isnull $objectType]
	if {$isObjectTypeNull == 1} {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_CONTENT_INVALID_REQUEST"
		$result_map put "message" "Object type filter is mandatory"
		$result_map put "responseCode" [java::new Integer 400]
		set response_list [create_error_response $result_map]
		return $response_list
	}
	
	
	set respDefNode [getDefinition $graphId $objectType]
	set respDefNodeError [check_response_error $respDefNode]
	if {$respDefNodeError} {
		return $respDefNode
	}
	
	set defNode [get_resp_value $respDefNode "definition_node"]
	
	set definitionMetadata [$defNode getMetadata]
	set weightagesString [$definitionMetadata get "weightages"]
	
	set isWeighatgesStringNull [java::isnull $weightagesString]
	if {$isWeighatgesStringNull == 1} {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_CONTENT_INVALID_REQUEST"
		$result_map put "message" "Weighatges metadata is not found for the object type"
		$result_map put "responseCode" [java::new Integer 400]
		set response_list [create_error_response $result_map]
		return $response_list
	}
	
	set weightagesMap [get_weightages_map $weightagesString]
	$baseConditions put "weightages" $weightagesMap
	$baseConditions put "graph_id" $graphId
	$baseConditions put "objectType" $objectType
	$request_map put "baseConditions" $baseConditions
}

$request_map put "filters" $filters

set searchResult [doLanguageSearch $request_map]
if {$wordChainsQuery == "false"} {
	set compositeSearchResponse [getCompositeSearchResponse $searchResult]
	return $compositeSearchResponse
}

set words [$searchResult get "results"]
set wordChainResponse [getWordChains $graphId $ruleObject $words $wordChainsLimit]
return $wordChainResponse
