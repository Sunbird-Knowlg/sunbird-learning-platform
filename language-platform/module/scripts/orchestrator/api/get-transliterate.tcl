package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map

set transliterate [java::new HashMap]

java::for {String language} $languages {

set transliterateResponse [transliterate $language [java::new Boolean true] $lemma]
set transliterateResultMap [$transliterateResponse getResult]
$transliterate put $language $transliterateResultMap
}

set result_map [java::new HashMap]
$result_map put "transliterations" $transliterate
set response_list [create_response $result_map]
return $response_list
