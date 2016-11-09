
package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.language.measures.entity WordComplexity


proc getThresholdLevel {word} {

	set filters [java::new HashMap]
	$filters put "objectType" "Word"
	$filters put "graph_id" "en"
	$filters put "lemma" $word
	$filters put "status" [java::new ArrayList]
	set limit [java::new Integer 1]

	set null_var [java::null]
	set empty_list [java::new ArrayList]
	set empty_map [java::new HashMap]

	set searchResponse [indexSearch $null_var $null_var $filters $empty_list $empty_list $empty_map $empty_list $null_var $limit]
	set searchResultsMap [$searchResponse getResult]
	set wordsList [java::cast List [$searchResultsMap get "results"]]
	set wordsListNull [java::isnull $wordsList]
	if {$wordsListNull == 1 || [$wordsList size] == 0} {
		return $null_var
	}

	set wordObject [java::cast Map [$wordsList get 0]]
	set thresholdLevel [$wordObject get "thresholdLevel"]
	return $thresholdLevel
}

set wordsDetailsResponse [java::new HashMap]

java::for {String word} $words {

	set wordDetails [java::new HashMap]
	set thresholdLevel [getThresholdLevel $word]
	$wordDetails put "threshold_level" $thresholdLevel
	set falseBool [java::new Boolean false]
	set transliteratedWordResponse [getPhoneticSpelling "hi" $falseBool $word]
	set check_error [check_response_error $transliteratedWordResponse]
	if {$check_error} {
		return $transliteratedWordResponse
	} 
	set transliteratedWord [get_resp_value $transliteratedWordResponse "phonetic_spelling"]
	set transliteratedWord [$transliteratedWord toString]
	$wordDetails put "hi_transliteration" $transliteratedWord
	set wordCompleixtyResponse [getWordFeatures "hi" $transliteratedWord]
	set check_error [check_response_error $wordCompleixtyResponse]
	if {$check_error} {
		return $wordCompleixtyResponse
	}
	set word_features [get_resp_value $wordCompleixtyResponse "word_features"]
	set wordFeatures [java::cast Map $word_features]
	set WordComplexity [$wordFeatures get $transliteratedWord]
	set WordComplexity [java::cast WordComplexity $WordComplexity]
	set phonicComplexity [java::prop $WordComplexity "phonicComplexity"]
	set syllablesCount [java::prop $WordComplexity "count"]
	$wordDetails put "phonologic_complexity" $phonicComplexity
	$wordDetails put "syllables_count" $syllablesCount
	$wordsDetailsResponse put $word $wordDetails
}

# create word chains response
set resp_object [java::new HashMap]
$resp_object put "words" $wordsDetailsResponse
set response [create_response $resp_object]
return $response

