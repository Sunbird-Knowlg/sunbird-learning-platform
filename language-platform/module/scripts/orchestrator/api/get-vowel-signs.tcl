package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set varna_object_type "Varna"
set varna_ipa_object_type "Varna_IPA"
set varna_ipa_graph_id "language"
set varna_type "Vowel Sign"

set searchProperty [java::new HashMap]
$searchProperty put "type" $varna_type

set property [create_search_property $searchProperty]

set search_response [getNodesByProperty $language_id $property]
set check_error [check_response_error $search_response]
if {$check_error} {
	puts "Error response from searchNodes"
	return $search_response;
} 
set resp_def_node [getDefinition $language_id $varna_object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]
set varna_ipa_resp_def_node [getDefinition $varna_ipa_graph_id $varna_ipa_object_type]
set varna_ipa_def_node [get_resp_value $varna_ipa_resp_def_node "definition_node"]
set graph_nodes [get_resp_value $search_response "node_list"]
set vowel_list [java::new ArrayList]

java::for {Node varna_node} $graph_nodes {
	set varna_obj [convert_graph_node $varna_node $def_node]
	set varnaIpaSymbol [$varna_obj get "ipaSymbol"]
	puts [$varnaIpaSymbol toString]

	set searchProperty [java::new HashMap]
	$searchProperty put "ipaSymbol" $varnaIpaSymbol

	set property [create_search_property $searchProperty]
	set search_response [getNodesByProperty $varna_ipa_graph_id $property]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		puts "Error response from searchNodes"
		return $varnaIpaSymbol;
	} 
	set graph_nodes [get_resp_value $search_response "node_list"]
	set varna_ipa_node [$graph_nodes get 0]

	set varna_ipa_obj [convert_graph_node $varna_ipa_node $varna_ipa_def_node]

	set phonoAttribVector [$varna_ipa_obj get "phonoAttribVector"]
	set audio [$varna_ipa_obj get "audio"]

	$varna_obj put "phonoAttribVector" $phonoAttribVector
	$varna_obj put "audio" $audio
	$vowel_list add $varna_obj
}
return $vowel_list


