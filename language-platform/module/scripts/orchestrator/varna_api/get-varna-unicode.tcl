package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set varna_object_type "Varna"
set varna_iso_object_type "Varna_ISO"
set varna_iso_graph_id "language"
set object_null [java::isnull $unicode]
if {$object_null == 1} {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_INVALID_REQUEST"
	$result_map put "message" "UNICODE IS MANDATORY"
	$result_map put "responseCode" [java::new Integer 400]
	set response_list [create_error_response $result_map]
	return $response_list
} 


set searchProperty [java::new HashMap]
$searchProperty put "varna" $unicode

set language_id [get_language_graph_id $unicode]
set property [create_search_property $searchProperty]

set search_response [getNodesByProperty $language_id $property]
set check_error [check_response_error $search_response]
if {$check_error} {
	puts "Error response from searchNodes"
	return $search_response;
} 
set graph_nodes [get_resp_value $search_response "node_list"]
set varna_node [$graph_nodes get 0]

set resp_def_node [getDefinition $language_id $varna_object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]
set varna_obj [convert_graph_node $varna_node $def_node]

set varnaIpaSymbol [$varna_obj get "isoSymbol"]

set isoIsNull [java::isnull $varnaIpaSymbol]
if {$isoIsNull == 0} {
	puts [$varnaIpaSymbol toString]

	set searchProperty [java::new HashMap]
	$searchProperty put "isoSymbol" $varnaIpaSymbol

	set property [create_search_property $searchProperty]

	set search_response [getNodesByProperty $varna_iso_graph_id $property]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		puts "Error response from searchNodes"
		return $search_response;
	} 
	set graph_nodes [get_resp_value $search_response "node_list"]
	set varna_iso_node [$graph_nodes get 0]

	set resp_def_node [getDefinition $varna_iso_graph_id $varna_iso_object_type]
	set def_node [get_resp_value $resp_def_node "definition_node"]

	set varna_iso_obj [convert_graph_node $varna_iso_node $def_node]

	set phonoAttribVector [$varna_iso_obj get "phonoAttribVector"]
	set audio [$varna_iso_obj get "audio"]

	$varna_obj put "phonoAttribVector" $phonoAttribVector
	$varna_obj put "audio" $audio
}
return $varna_obj


