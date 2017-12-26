package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.language.measures.entity WordComplexity
java::import -package org.ekstep.graph.dac.model Node

proc getWordMap {words} {
	set filters [java::new HashMap]
	$filters put "objectType" "Word"
	$filters put "graph_id" "en"
	$filters put "lemma" $words
	$filters put "status" [java::new ArrayList]
	set limit [java::new Integer 10000]

	set null_var [java::null]
	set empty_list [java::new ArrayList]
	set empty_map [java::new HashMap]

	set searchResponse [indexSearch $null_var $null_var $filters $empty_list $empty_list $empty_map $empty_list $null_var $null_var $limit]
	set searchResultsMap [$searchResponse getResult]
	set wordsList [java::cast List [$searchResultsMap get "results"]]
	set wordsListNull [java::isnull $wordsList]
	set wordMap [java::new HashMap]
	if {$wordsListNull == 0 && [$wordsList size] >= 0} {
		java::for {Object wordObj} $wordsList {
			set wordObject [java::cast Map $wordObj]	
			set lemma [$wordObject get "lemma"]	
			$wordMap put $lemma $wordObject
		}	
	}
	return $wordMap
}

set wordMap [getWordMap $words]
java::for {String word} $words {
	set wordObj [$wordMap get $word]
	set isWordNull [java::isnull $wordObj]
	if {$isWordNull == 1} {
		set graph_node [java::new Node [java::null] "DATA_NODE" "Word"]
		set metadata [java::new HashMap]
		set word [java::new String $word]
		$metadata put "lemma" [$word toLowerCase]
		$metadata put "thresholdLevel" $level
		$graph_node setMetadata $metadata
		puts "creating word - $word"
		set create_response [createDataNode $language_id $graph_node]
	} else {
		set wordObject [java::cast Map $wordObj]
		set identifier [$wordObject get "identifier"]
		set identifier [$identifier toString]
		set thresholdLevel [$wordObject get "thresholdLevel"]
		set thresholdLevelNull [java::isnull $thresholdLevel]
		if {$thresholdLevelNull == 1} {
			set graph_node [java::new Node $identifier "DATA_NODE" "Word"]
			set metadata [java::new HashMap]
			$metadata put "thresholdLevel" $level
			$graph_node setMetadata $metadata
			puts "updating word - $word"
			set create_response [updateDataNode $language_id $identifier $graph_node]
		}
	}
}
return "threshold level updated"

