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
}

set isLimitNull [java::isnull $limit]
if {$isLimitNull == 0} {
	set wordChainsLimit $limit
}

set isLanguageIdNull [java::isnull $languageId]
if {$isLanguageIdNull == 0} {
	set languageIdSize [$languageId size]
	if {$languageIdSize > 0} {
		set language [$languageId get 0]
		set graphId $language
	}
}

$request_map put "filters" $filters
$request_map put "query" $query
$request_map put "exists" $exists
$request_map put "not_exists" $not_exists
$request_map put "sort_by" $sort_by
$request_map put "facets" $facets
$request_map put "limit" $limit
$request_map put "fuzzy" $fuzzy

if {$wordChainsQuery == "true"} {
	if {isLanguageIdNull == 1 || $languageIdSize == 0} {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_CONTENT_INVALID_REQUEST"
		$result_map put "message" "At least one language Id is mandatory"
		$result_map put "responseCode" [java::new Integer 400]
		set response_list [create_error_response $result_map]
		return $response_list
	}
	
	$request_map put "traversal" $wordChainsQuery
	
	set get_rule_response [getDataNode $graph_id $traversalId]
	set get_rule_response_error [check_response_error $get_rule_response]
	if {$get_rule_response_error} {
		return $get_rule_response
	}
	
	set ruleNode [get_resp_value $get_rule_response "node"]
	
	set rule_def_node [getDefinition $graph_id $traversalRuleDefinition]
	set def_node [get_resp_value $rule_def_node "definition_node"]

	set ruleObject [convert_graph_node $ruleNode $rule_def_node]
	
	set ruleMetadata [$ruleNode getMetadata]
	
	set $objectType [$ruleMetadata get "ruleObjectType"]
	set $searchResultsLimit [$ruleMetadata get "wordChainWordsSize"]
	
	$request_map put "limit" $searchResultsLimit
}

if {$fuzzySearch == "true"} {
	set isObjectTypeNull [java::isnull $objectType]
	if {$isObjectTypeNull == 1} {
		set objectType [$filters get "objectType"]
	}
	
	set $isObjectTypeNull [java::isnull $objectType]
	if {$isObjectTypeNull == 0} {
		set objectType [$filters get "objectType"]
	}
	
	set respDefNode [getDefinition $graphId $objectType]
	set defNode [get_resp_value $respDefNode "definition_node"]
	
	set definitionMetadata [$defNode getMetadata]
	set weightagesString [$definitionMetadata get "weightages"]
	set weightagesMap [get_weightages_map $weightagesString]
	$baseConditions put "weightages" $weightagesMap
	$baseConditions put "graph_id" $graphId
	$baseConditions put "objectType" $objectType
	$request_map put "baseConditions" $baseConditions
}

set searchResult [doLanguageSearch $request_map]
if {$wordChainsQuery == "false"} {
	set compositeSearchResponse [getCompositeSearchResponse $searchResult]
	return $compositeSearchResponse
}

set words [$searchResult "results"]
set wordChainResponse [getWordChains $graphId $ruleObject $words $wordChainsLimit]
return $wordChainResponse
