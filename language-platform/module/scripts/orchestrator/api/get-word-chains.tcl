package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package com.ilimi.graph.dac.model Node

set maxDefinedDepth [$ruleObject get "maxChainLength"]
set minDefinedDepth [$ruleObject get "minChainLength"]
set startWordsSize [$ruleObject get "startWordsSize"]
set ruleType [$ruleObject get "type"]
set ruleScript [$ruleObject get "ruleScript"]

set wordsSize [$words size]

if {$wordsSize > $startWordsSize} {
	set topWords [$words subList 0 $startWordsSize]
} else {
	set topWords [$words subList 0 $startWordsSize]
}

set ids [java::new ArrayList]
set wordScore [java::new HashMap]
set wordIdMap [java::new HashMap]

java::for {Map word} $words {
	set id [$word get "identifier"]
	$ids add $id
	set score [$word get "score"]
	$wordScore put $id $score
	$wordIdMap put $id $word
}

set ruleScriptString [$ruleScript toString]
set wordChainsResponse [$ruleScriptString $graphId $topWords $ids $maxDefinedDepth $minDefinedDepth $wordScore $wordChainsLimit]

set finalWordChains [$wordChainsResponse get "result"]

set finalWordIds [java::new HashSet]
java::for {Map wordChain} $finalWordChains {
	set wordIdsObject [$wordChain get "list"]
	set wordIds [java::cast ArrayList $wordIdsObject]
	$finalWordIds addAll $wordIds
}

set wordChainWords [java::new ArrayList]

java::for {String wordId} $finalWordIds {
	set wordFromMap [$wordIdMap get $wordId]
	$wordChainWords add $wordFromMap
}

set resp_object [java::new HashMap]
$resp_object put "words" $wordChainWords
$resp_object put "relations" $finalWordChains
set response [create_response $resp_object]
return $response