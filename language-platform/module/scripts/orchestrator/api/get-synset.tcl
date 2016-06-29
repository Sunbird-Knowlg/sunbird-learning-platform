package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set lemma_list [java::new ArrayList]
set object_type "Synset"
set error_status "Failed"

set contains_response [containsLanguage $language_id]
set contains_response_error [check_response_error $contains_response]
if {$contains_response_error} {
	return $contains_response;
}
set result [$contains_response get "result"]
set lang_eqs [$result equals "true"]
if {!$lang_eqs} {
	set result_map [java::new HashMap]
	$result_map put "code" "INVALID_LANGUAGE"
	$result_map put "message" "INVALID LANGUAGE"
	$result_map put "responseCode" [java::new Integer 400]
	set err_response [create_error_response $result_map]
	return $err_response
}

set get_node_response [getDataNode $language_id $synset_id]
return $get_node_response

