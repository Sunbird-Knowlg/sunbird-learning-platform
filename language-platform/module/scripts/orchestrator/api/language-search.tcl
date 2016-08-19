#This script acts as the entry point for langugae search
#Input: Query, Filters, exists, not_exits, facets, sort_by and limit
#Output: Search results, if its not a word chain request; Word chains (words and relations) if its a word chain request


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
set weightagesMap [java::new HashMap]

# check if its a fuzzy search
set isFuzzyNull [java::isnull $fuzzy]
if {$isFuzzyNull == 0} {
	set fuzzyString [$fuzzy toString]
	set fuzzySearch $fuzzyString
}

# check if its a traversal based search
set isTraversalIdNull [java::isnull $traversals]
if {$isTraversalIdNull == 0} {
	# set fuzzySearch "true"
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

set isFiltersNull [java::isnull $filters]
if {$isFiltersNull == 1} {
	set filters [java::new HashMap]
}

set languageIdObj [$filters get "language_id"]
set languageId [java::cast ArrayList $languageIdObj]

# Set Graph Id from filter
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

#copy request parameters for Search request
$request_map put "query" $query
$request_map put "exists" $exists
$request_map put "not_exists" $not_exists
$request_map put "sort_by" $sort_by
$request_map put "facets" $facets
$request_map put "limit" $limit
$request_map put "fuzzy" $fuzzy

# enhance request object for traversal
if {$wordChainsQuery == "true"} {
	if {$isLanguageIdNull == 1 || $languageIdSize == 0} {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_CONTENT_INVALID_REQUEST"
		$result_map put "message" "At least one language Id is mandatory"
		$result_map put "responseCode" [java::new Integer 400]
		set response_list [create_error_response $result_map]
		return $response_list
	}

	$request_map put "traversal" [java::new Boolean "true"]

	#get rule node for traversal
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

	#get objectType from rule node
	set objectType [$ruleObject get "ruleObjectType"]
	set isObjectTypeNull [java::isnull $objectType]
	if {$isObjectTypeNull == 0} {
		$filters put "objectType" $objectType
	}

	set searchResultsLimit [$ruleObject get "wordChainWordsSize"]

	set limit $searchResultsLimit
}

#add the updated filters back to the request
$request_map put "filters" $filters

#do the search on elasticsearch
set searchResponse [indexSearch $traversals $query $filters $exists $not_exists $sort_by $facets $fuzzy $limit]
set searchResultsMap [$searchResponse getResult]

#if its not a traversal search, group results by object type and return
if {$wordChainsQuery == "false"} {
	set compositeSearchResponse [groupSearchResultByObjectType $searchResultsMap]
	return $compositeSearchResponse
}

#if its a traversal search, retreive results and form word chains
set words [$searchResultsMap get "results"]
set wordChainResponse [getWordChains $graphId $ruleObject $words $wordChainsLimit]
return $wordChainResponse
