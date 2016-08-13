package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package com.ilimi.graph.dac.model Node
java::import -package com.ilimi.graph.dac.model Path
java::import -package org.ekstep.language.wordchain.evaluators WordIdEvaluator

proc processPath {finalPath wordScore relation} {
	set wordChain [java::new ArrayList]
	set totalScore 0
	set averageScore 0.0
	set chainLength 0
	set wordChainRecord [java::new HashMap]
	set title ""
	set pathNodes [$finalPath getNodes]
	set wordObjectType "Word"
	java::for {Node node} $pathNodes {
		set nodeMetadata [$node getMetadata]
		set nodeObjectType [$node getObjectType]
		
		set pb "No"
		set objectTypeEqualsWordString "false"
		if {$nodeObjectType == "Word"} {
			set objectTypeEqualsWordString "true"
		} else {
			set pb "Yes"
		}
		
		
		#set objectTypeEqualsWord [$nodeObjectType equalsIgnoreCase $wordObjectType]
		#set objectTypeEqualsPB [$nodeObjectType equalsIgnoreCase "Phonetic_Boundary"]
		#set objectTypeEqualsWordString [$objectTypeEqualsWord toString]
		
		if {$objectTypeEqualsWordString == "true"} {
			set nodeIdentifier [$node getIdentifier]
			set wordChainContains [$wordChain contains $nodeIdentifier]
			if {$wordChainContains == 1} {
				return;
			}
			$wordChain add $nodeIdentifier
			set score [$wordScore get $nodeIdentifier]
			set scoreString [$score toString]
			set totalScore [expr $totalScore + $scoreString]
			set chainLength [expr $chainLength + 1]
			
		}		
	}
	
	set wordChainSize [$wordChain size]
	if {$wordChainSize == 0} {
		return [java::null]
	}
	
	set averageScore [expr $totalScore/$chainLength]
	$wordChainRecord put "title" $title
	$wordChainRecord put "list" $wordChain
	$wordChainRecord put "score" $averageScore
	$wordChainRecord put "relation" $relation
	return $wordChainRecord
}

set maxDefinedDepthObj [$ruleObject get "maxChainLength"]
set maxDefinedDepth [$maxDefinedDepthObj toString]

set minDefinedDepthObj [$ruleObject get "minChainLength"]
set minDefinedDepth [$minDefinedDepthObj toString]

set startWordsSize [$ruleObject get "startWordsSize"]
set ruleType [$ruleObject get "type"]
set ruleScript [$ruleObject get "ruleScript"]

set wordsSize [$searchResult size]

set startWordsSizeString [$startWordsSize toString]

if {$wordsSize > $startWordsSizeString} {
	set topWords [$searchResult subList 0 $startWordsSize]
} else {
	set topWords $searchResult
}

set topWordsList [java::cast List $topWords]

set ids [java::new ArrayList]
set wordScore [java::new HashMap]
set wordIdMap [java::new HashMap]


java::for {Map word} $searchResult {
	set id [$word get "identifier"]
	$ids add $id
	set score [$word get "score"]
	$wordScore put $id $score
	$wordIdMap put $id $word
}


set ruleType "Akshara Rule"
set wordChains [java::new ArrayList]

set maxDepth [expr {3 * ($maxDefinedDepth-1)}]
set minDepth [expr {3 * ($minDefinedDepth-1)}]

set relationTypes [java::new ArrayList]
$relationTypes add "hasMember"
$relationTypes add "startsWithAkshara"
$relationTypes add "hasMember"

set directions [java::new ArrayList]
$directions add "INCOMING"
$directions add "OUTGOING"
$directions add "OUTGOING"

set nodeCount 3

set pathExpander [java::new HashMap]
$pathExpander put "relationTypes" $relationTypes
$pathExpander put "directions" $directions
$pathExpander put "nodeCount" $nodeCount

set traversalUniqueness [java::new ArrayList]
#$traversalUniqueness add "NODE_GLOBAL"
$traversalUniqueness add "RELATIONSHIP_GLOBAL"

set wordIdEval [java::new WordIdEvaluator $ids]

set evaluators [java::new ArrayList]
$evaluators add $wordIdEval

set traversalRequest [java::new HashMap]
$traversalRequest put "pathExpander" $pathExpander
$traversalRequest put "uniqueness" $traversalUniqueness
$traversalRequest put "minLength" $minDepth
$traversalRequest put "maxLength" $maxDepth
$traversalRequest put "evaluators" $evaluators
set traverser [get_traverser $graphId $traversalRequest]

java::for {Map topWord} $topWords {
	set topWordIdObject [$topWord get "identifier"]
	set topWordId [$topWordIdObject toString]
	$traverser setStartNode $topWordId
	set resp_traverse [traverse $graphId $traverser]
	set check_error [check_response_error $resp_traverse]
	if {$check_error} {
		return $resp_traverse;
	} 
	
	set subGraph [get_resp_value $resp_traverse "sub_graph"]
	set paths [$subGraph getPaths]
	java::for {Path finalPath} $paths {
		set wordChain [processPath $finalPath $wordScore $ruleType]
		if {$wordChain != ""} { 
			set isWordChainNull [java::isnull $wordChain]
			if {$isWordChainNull == 0} {
				$wordChains add $wordChain
			}
		} 
	}
}


set sortedWordChains [sort_maps $wordChains "score" "DESC"]
set finalWordChains [java::new ArrayList]
set wordChainsSize [$sortedWordChains size]
set wordChainsLimitString [$wordChainsLimit toString]
if { $wordChainsSize > $wordChainsLimitString} {
	set finalWordChains [$sortedWordChains subList 0 $wordChainsLimit]
} else {
	set finalWordChains $sortedWordChains
}

return $finalWordChains