#Invokes the rule script and forms the final word chains response
#Input
# 1. Graph Id - language
# 2. Rule object - Map<String, Object>
# 3. Search Result - (List<Map<String, Object>>) (List of words)
# 4. Word Chains Limit
#
#Output
# 1. Words - List<Map<String, Object>> - List of word objects
# 2. Relations - List<Map<String, Object>> - List of word chains

package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package com.ilimi.graph.dac.model Node

#get ruleScript name from the rule Object
set ruleScript [$ruleObject get "ruleScript"]

set ruleScriptString [$ruleScript toString]

#invoke ruleScript
#ex: getAksharaRuleWordChains tcl script
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
set wordChainsResponse [$ruleScriptString $graphId $ruleObject $searchResult $wordChainsLimit]
set finalWordChains [$wordChainsResponse get "result"]

#collect unique words from the result
set finalWordIds [java::new HashSet]
java::for {Map wordChain} $finalWordChains {
	set wordIdsObject [$wordChain get "list"]
	set wordIds [java::cast ArrayList $wordIdsObject]
	$finalWordIds addAll $wordIds
}

set wordChainWords [java::new ArrayList]
set wordIdMap [java::new HashMap]

#form WordId to Word Object Map
java::for {Map word} $searchResult {
	set id [$word get "identifier"]
	$wordIdMap put $id $word
}

# form word objects list from list of unique words 
java::for {String wordId} $finalWordIds {
	set wordFromMap [$wordIdMap get $wordId]
	$wordChainWords add $wordFromMap
}

# create word chains response
set resp_object [java::new HashMap]
$resp_object put "words" $wordChainWords
$resp_object put "relations" $finalWordChains
set response [create_response $resp_object]
return $response