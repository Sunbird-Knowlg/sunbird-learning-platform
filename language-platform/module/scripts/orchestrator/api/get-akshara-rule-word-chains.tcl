#This script does the following
# 1. Create traversal description using relations
# 2. Traverses the graph and returns paths
# 3. Scores the paths
# 4. Creates word chain response
# 5. Sorts results based on score in desc order
# 6. returns results
#
#Input:
#1. Graph Id (language)
#2. Rule Object (Map<String, Object>)
#3. Search Result (List<Map<String, Object>>) (List of words)
#4. Word Chains Limit
#
#Output
# 1. Word Chains - List<Map<String, Object>>
# ex: [
#       {
#			"title": "A T",
#			"list": ["en_1", "en_2"],
#			"score": 2.0,
#			"relation": "Akshara"
#		}
#     ]

package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package com.ilimi.graph.dac.model Node
java::import -package com.ilimi.graph.dac.model Path
java::import -package org.ekstep.language.wordchain.evaluators WordIdEvaluator
java::import -package com.ilimi.common.dto Response

#given a path, word scores and relation, scores the word chain 
#and forms the word chain data structure
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

#get the top words
if {$wordsSize > $startWordsSizeString} {
	set topWords [$searchResult subList 0 $startWordsSize]
} else {
	set topWords $searchResult
}

set topWordsList [java::cast List $topWords]

set ids [java::new ArrayList]
set wordScore [java::new HashMap]
set wordIdMap [java::new HashMap]

# form wordId to Word Object Map
# form wordId to Score Map
java::for {Map word} $searchResult {
	set id [$word get "identifier"]
	$ids add $id
	set score [$word get "score"]
	set isScoreNull [java::isnull $score]
	if {$isScoreNull == 1} {
		set score 1.0
	}
	$wordScore put $id $score
	$wordIdMap put $id $word
}

set ruleType "Akshara Rule"
set wordChains [java::new ArrayList]

# the no of nodes after start node that forms a chain of two words is 3 for Phonetic boundary
# increase the min and max length to include the internal nodes
set maxDepth [expr {3 * ($maxDefinedDepth-1)}]
set minDepth [expr {3 * ($minDefinedDepth-1)}]

#create a list of relations in the required traversal order
set relationTypes [java::new ArrayList]
$relationTypes add "hasMember"
$relationTypes add "follows"
$relationTypes add "hasMember"

#create a list of dierctions in the required traversal order
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

set commands [java::new ArrayList]

java::for {Map topWord} $topWords {
	set topWordIdObject [$topWord get "identifier"]
	set topWordId [$topWordIdObject toString]
	$traverser setStartNode $topWordId
	
	set commandMap [java::new HashMap]
	$commandMap put "commandName" "traverse"
	set commandParams [java::new HashMap]
	$commandParams put "graph_id" $graphId
	$commandParams put "traversal_description" $traverser
	
	$commandMap put "commandParams" $commandParams
	$commands add $commandMap
}

set resp_traverse [execute_commands_concurrently $commands]

set responses [get_resp_value $resp_traverse "responses"]
java::for {Response response} $responses {
	set check_error [check_response_error $response]
	if {$check_error} {
		return $response;
	} 

	set subGraph [get_resp_value $response "sub_graph"]
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