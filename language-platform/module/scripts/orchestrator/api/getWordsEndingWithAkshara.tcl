
package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package java.util Collection
java::import -package com.ilimi.graph.dac.model Node Relation

# get list of aksharas from rule object
set aksharaList [$ruleObject get "aksharas"]

# get rule name from rule object
set ruleName [$ruleObject get "identifier"]

# get maxChainLength from rule object and set it as maxChainLength
set maxChainLengthObj [$ruleObject get "maxChainLength"]
set maxChainLength [$maxChainLengthObj toString]

# get minChainLength from rule object and set it as minChainLength
set minChainLengthObj [$ruleObject get "minChainLength"]
set minChainLength [$minChainLengthObj toString]

# get syllableCount from rule object and use it for syllableCount filter
set syllableCount [$ruleObject get "syllableCount"]

set statusList [java::new ArrayList]
$statusList add "Live"
$statusList add "Draft"

set null_var [java::null]
set empty_list [java::new ArrayList]
set empty_map [java::new HashMap]

set words [java::new ArrayList]
set wordIdList [java::new ArrayList]
set relations [java::new ArrayList]
java::for {Object aksharaObj} $aksharaList {
	set akshara [$aksharaObj toString]
	set filters [java::new HashMap]
	$filters put "objectType" "Word"
	$filters put "graph_id" $graphId

	set lemmaMap [java::new HashMap]
	$lemmaMap put "endsWith" $akshara
	$filters put "lemma" $lemmaMap
	$filters put "status" $statusList
	$filters put "syllableCount" $syllableCount
	set limit [java::new Integer $maxChainLength]

	set searchResponse [indexSearch $null_var $null_var $filters $empty_list $empty_list $empty_map $empty_list $null_var $null_var $limit]
	set searchResultsMap [$searchResponse getResult]

	set wordList [$searchResultsMap get "results"]
	set relation [java::new HashMap]
	$relation put "relation" $ruleName
	set totalScore 0
	set list [java::new ArrayList]
	java::for {Map word} $wordList {
		set id [$word get "identifier"]
		set wordAdded [$wordIdList contains $id]
		if {$wordAdded == 0} {
			$words add $word
			$wordIdList add $id	
		}
		$list add $id
		set score [$word get "score"]
		set isScoreNull [java::isnull $score]
		if {$isScoreNull == 1} {
			set score 1.0
		}
		set totalScore [expr $totalScore + $score]
	}
	$relation put "list" $list
	$relation put "score" $totalScore
	$relations add $relation
}

set wordChains [java::new ArrayList]

# filter wordChain that meets minimum word chain length
java::for {Map wordChain} $relations {
	set wordList [$wordChain get "list"]
	set wordList [java::cast List $wordList]
	set wordChainLength [$wordList size]
	if {$wordChainLength >= $minChainLength} {
		$wordChains add $wordChain
	}
}

# sort the wordChains based on score(average)
set sortedWordChains [sort_maps $wordChains "score" "DESC"]
set finalWordChains [java::new ArrayList]
set wordChainsSize [$sortedWordChains size]
set wordChainsLimitString [$wordChainsLimit toString]
if { $wordChainsSize > $wordChainsLimitString} {
	# sublist sortedWordChains based on given wordChainsLimit
	set finalWordChains [$sortedWordChains subList 0 $wordChainsLimit]
} else {
	set finalWordChains $sortedWordChains
}

set result_map [java::new HashMap]
$result_map put "words" $words
$result_map put "relations" $finalWordChains
set response_list [create_response $result_map]
return $response_list

