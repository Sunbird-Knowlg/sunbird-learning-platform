package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map

set transliterate [java::new HashMap]

puts "test $languages #lemma"
java::for {String language} $languages {

if {$language == "en"} {
	continue
}

set transliterateResponse [transliterate $language [java::new Boolean "true"] $lemma]

set get_node_response_error [check_response_error $transliterateResponse]
if {$get_node_response_error} {
	continue
}
set transliterateResultMap [$transliterateResponse getResult]
set transliterateResultMap [java::cast Map $transliterateResultMap]
set outputText [$transliterateResultMap get "output"]
set outputText [$outputText toString]
set outputText [string trim $outputText]

if { $outputText==$lemma } {
	#skip if output is same as input lemma
} else {
	$transliterateResultMap put "output" $outputText
	$transliterate put $language $transliterateResultMap	
}

}

set result_map [java::new HashMap]
$result_map put "transliterations" $transliterate
set response_list [create_response $result_map]
return $response_list
