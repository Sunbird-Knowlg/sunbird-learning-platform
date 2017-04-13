package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

proc getIntValue {obj} {
	set isObjNull [java::isnull $obj]
	if {$isObjNull == 0} {
		set long_inst [java::instanceof $obj Long]
		if {$long_inst == 1} {
			set long_obj [java::cast Long $obj]
			return [$long_obj intValue]
		} 
		set int_inst [java::instanceof $obj Integer]
		if {$int_inst == 1} {
			set int_obj [java::cast Integer $obj]
			return [$int_obj intValue]
		}
	}
	return 0
}

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
set words [getIntValue [$resp_object get "words"]]
set updatedWordsCount [expr $words + $wordCount]

if {[java::isnull $liveWordCount] == 1} {
	set liveWordCount [java::new Integer 0]
}

set liveWordCount [$liveWordCount intValue]
set liveWords [getIntValue [$resp_object get "liveWords"]]
set updatedLiveWordsCount [expr $liveWords + $liveWordCount]
$resp_object put "words" [java::new Integer $updatedWordsCount]
$resp_object put "liveWords" [java::new Integer $updatedLiveWordsCount]
$resp_object put "objectType" "Language"
set language_obj [convert_to_graph_node $resp_object $def_node]
set create_response [updateDataNode "domain" $lang_node_id $language_obj]
return $create_response


