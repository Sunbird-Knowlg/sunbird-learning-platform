package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package com.ilimi.graph.dac.model Node

set ruleScript [$ruleObject get "ruleScript"]

set ruleScriptString [$ruleScript toString]
set wordChainsResponse [$ruleScriptString $graphId $ruleObject $searchResult $wordChainsLimit]

set finalWordChains [$wordChainsResponse get "result"]

set finalWordIds [java::new HashSet]
java::for {Map wordChain} $finalWordChains {
	set wordIdsObject [$wordChain get "list"]
	set wordIds [java::cast ArrayList $wordIdsObject]
	$finalWordIds addAll $wordIds
}

set wordChainWords [java::new ArrayList]
set wordIdMap [java::new HashMap]


java::for {Map word} $searchResult {
	set id [$word get "identifier"]
	$wordIdMap put $id $word
}

java::for {String wordId} $finalWordIds {
	set wordFromMap [$wordIdMap get $wordId]
	$wordChainWords add $wordFromMap
}

set resp_object [java::new HashMap]
$resp_object put "words" $wordChainWords
$resp_object put "relations" $finalWordChains
set response [create_response $resp_object]
return $response