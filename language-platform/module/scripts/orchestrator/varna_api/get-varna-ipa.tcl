package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set lemma_list [java::new ArrayList]
set object_type "Varna_IPA"
set error_status "Failed"
set language_id "language"

set get_node_response [getDataNode $language_id $ipa_id]
set get_node_response_error [check_response_error $get_node_response]
if {$get_node_response_error} {
	puts "Error response from getDataNode"
	return $get_node_response
}

set resp_def_node [getDefinition $language_id $object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]

set varnaIPA_node [get_resp_value $get_node_response "node"]
set varnaIPA_obj [convert_graph_node $varnaIPA_node $def_node]

set result_map [java::new HashMap]
$result_map put "varnaIPA" $varnaIPA_obj
set api_response [create_response $result_map]
return $api_response

