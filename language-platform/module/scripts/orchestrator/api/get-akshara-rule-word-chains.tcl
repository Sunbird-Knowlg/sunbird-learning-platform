package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package com.ilimi.graph.dac.model Node
java::import -package com.ilimi.graph.dac.model Path

proc processPath {finalPath wordScore relation} {
	set wordChain [java::new ArrayList]
	set totalScore 0.0
	set averageScore 0.0
	set chainLength 0
	set wordChainRecord [java::new HashMap]
	set title ""
	set pathNodes [path getNodes]
	java::for {Node node} $pathNodes {
		set nodeMetadata [$node getMetadata]
		set nodeObjectType [$node getObjectType]
		set objectTypeEqualsWord [$nodeObjectType equalsIgnoreCase "Word"]
		set objectTypeEqualsPB [$nodeObjectType equalsIgnoreCase "Phonetic_Boundary"]
		if {objectTypeEqualsWord == true} {
			set nodeIdentifier [$node getIdentifier]
			set wordChainContains [$wordChain contains $nodeIdentifier]
			if {wordChainContains == true} {
				return;
			}
			$wordChain add $nodeIdentifier
			set score [$wordScore get $nodeIdentifier]
			set $totalScore [$totalScore+$score]
			set $chainLength [$chainLength+1]
		}		
	}
	set wordChainSize [$wordChain size]
	if {$wordChainSize == 0} {
		return [java::null]
	}
	
	set averageScore [$totalScore/$chainLength]
	$wordChainRecord put "title" $title
	$wordChainRecord put "list" $wordChain
	$wordChainRecord put "score" $averageScore
	$wordChainRecord put "relation" $relation
	return $wordChainRecord
}

set ruleType "Akshara Rule"
set wordsSize [$words size]
set wordChains [java::new ArrayList]

set maxDepth [$maxDefinedDepth + ($maxDefinedDepth - 1)]
set minDepth [$minDefinedDepth + ($minDefinedDepth - 1)]

set $traversalRelations [java::new HashMap]
$traversalRelations put "hasMember" "BOTH"
$traversalRelations put "startsWithAkshara" "OUTGOING"

set $traversalUniqueness [java::new ArrayList]
$traversalUniqueness add "NODE_GLOBAL"
$traversalUniqueness add "RELATIONSHIP_GLOBAL"

java::for {Map topWord} $topWords {
	set topWordId [$topWord get "identifier"]
	set traverser [get_traverser $graphId $traversalRelations $traversalUniqueness $minDepth $maxDepth $topWordId]
	set resp_traverse [traverse $traverser]
	set check_error [check_response_error $resp_traverse]
	if {$check_error} {
		return $resp_traverse;
	} 
	
	set subGraph [get_resp_value $resp_traverse "sub_graph"]
	set paths [$subGraph getPaths]
	java::for {Path finalPath} $paths {
		set wordChain [processPath $finalPath $wordScore $ruleType]
		set isWordChainNull [java::isnull $wordChain]
		if {$isWordChainNull == 0} {
			$wordChains add $wordChain
		}
	}
}

set sortedWordChains [sortMap $wordChains "score" "desc"]

set finalWordChains [java::new ArrayList]
set wordChainsSize [$sortedWordChains size]

if {$wordChainsSize > $wordChainsLimit} {
	set $finalWordChains [$sortedWordChains sublist 0 $wordChainsLimit]
} else {
	set $finalWordChains $sortedWordChains
}

return $finalWordChains