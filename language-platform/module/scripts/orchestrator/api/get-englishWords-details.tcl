
package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.language.measures.entity WordComplexity


proc getThresholdLevel {words} {
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
	set thresholdLevelMap [java::new HashMap]
	if {$wordsListNull == 0 && [$wordsList size] >= 0} {
		java::for {Object wordObj} $wordsList {
			set wordObject [java::cast Map $wordObj]		
			set thresholdLevel [$wordObject get "thresholdLevel"]
			set lemma [$wordObject get "lemma"]
			$thresholdLevelMap put $lemma $thresholdLevel
		}	
	}
	return $thresholdLevelMap
}

set thresholdLevelMap [getThresholdLevel $words]
set wordsDetailsResponse [java::new ArrayList]
java::for {String word} $words {
	set wordDetails [java::new HashMap]
	$wordDetails put "word" $word
	set word [[java::new String $word] toLowerCase]
	set thresholdLevel [$thresholdLevelMap get $word]
	$wordDetails put "thresholdLevel" $thresholdLevel
	set falseBool [java::new Boolean true]
	set transliteratedWordResponse [getPhoneticSpelling "hi" $falseBool $word]
	set check_error [check_response_error $transliteratedWordResponse]
	if {$check_error} {
		return $transliteratedWordResponse
	} 
	set transliteratedWord [get_resp_value $transliteratedWordResponse "phonetic_spelling"]
	set transliteratedWord [$transliteratedWord toString]
	$wordDetails put "hindi" $transliteratedWord
	set wordCompleixtyResponse [getWordFeatures "hi" $transliteratedWord]
	set check_error [check_response_error $wordCompleixtyResponse]
	if {$check_error} {
		return $wordCompleixtyResponse
	}
	set word_features [get_resp_value $wordCompleixtyResponse "word_features"]
	set wordFeatures [java::cast Map $word_features]
	set wordComplexity [$wordFeatures get $transliteratedWord]
	set wordComplexityNull [java::isnull $wordComplexity]
	if {$wordComplexityNull == 0} {
		set wordComplexity [java::cast WordComplexity $wordComplexity]
		set phonicComplexity [java::prop $wordComplexity "phonicComplexity"]
		set syllablesCount [java::prop $wordComplexity "count"]
		$wordDetails put "phonological_complexity" $phonicComplexity
		$wordDetails put "syllableCount" $syllablesCount	
	}
	$wordsDetailsResponse add $wordDetails
}

# create word chains response
set resp_object [java::new HashMap]
$resp_object put "words" $wordsDetailsResponse
set response [create_response $resp_object]
set response_csv [convert_response_to_csv $response "words"]
return $response_csv

