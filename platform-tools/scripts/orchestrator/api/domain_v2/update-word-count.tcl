package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set lemma_list [java::new ArrayList]
set object_type "Language"
set error_status "Failed"

set prefix "lang_"
set lang_node_id [concat $prefix$language_id]
set get_node_response [getDataNode "domain" $lang_node_id]
set graph_node [get_resp_value $get_node_response "node"]

set resp_def_node [getDefinition "domain" "Language"]
set def_node [get_resp_value $resp_def_node "definition_node"]
set resp_object [convert_graph_node $graph_node $def_node]

if {[java::isnull $wordCount] == 1} {
	set wordCount [java::new Integer 0]
}

set wordCount [$wordCount intValue]

set ogWords [java::new Integer [$resp_object get "words"]]
set words [$ogWords intValue]
set updatedWordsCount [expr $words + $wordCount]

if {[java::isnull $liveWordCount] == 1} {
	set liveWordCount [java::new Integer 0]
}

set liveWordCount [$liveWordCount intValue]

set ogLiveWordsObj [$resp_object get "liveWords"]
if {[java::isnull $ogLiveWordsObj] == 1} {
	set ogLiveWords [java::new Integer 0]
} else {
	set ogLiveWords [java::new Integer [$resp_object get "liveWords"]]
}

set liveWords [$ogLiveWords intValue]
set updatedLiveWordsCount [expr $liveWords + $liveWordCount]
$resp_object put "words" [java::new Integer $updatedWordsCount]
$resp_object put "objectType" "Language"
set language_obj [convert_to_graph_node $resp_object $def_node]
set create_response [updateDataNode "domain" $lang_node_id $language_obj]
return $create_response


