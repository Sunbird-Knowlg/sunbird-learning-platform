package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set varna_object_type "Varna"
set varna_iso_object_type "Varna_ISO"
set varna_iso_graph_id "language"

set searchProperty [java::new HashMap]
$searchProperty put "type" $varna_type

set property [create_search_property $searchProperty]

set search_response [getNodesByProperty $language_id $property]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} 
set resp_def_node [getDefinition $language_id $varna_object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]
set varna_iso_resp_def_node [getDefinition $varna_iso_graph_id $varna_iso_object_type]
set varna_iso_def_node [get_resp_value $varna_iso_resp_def_node "definition_node"]
set graph_nodes [get_resp_value $search_response "node_list"]
set varnas_list [java::new ArrayList]

java::for {Node varna_node} $graph_nodes {
	set varna_obj [convert_graph_node $varna_node $def_node]
	set varnaIpaSymbol [$varna_obj get "isoSymbol"]
	set isoIsNull [java::isnull $varnaIpaSymbol]
	if {$isoIsNull == 0} {
		set searchProperty [java::new HashMap]
		$searchProperty put "isoSymbol" $varnaIpaSymbol

		set property [create_search_property $searchProperty]
		set search_response [getNodesByProperty $varna_iso_graph_id $property]
		set check_error [check_response_error $search_response]
		if {!$check_error} {
			set graph_nodes [get_resp_value $search_response "node_list"]
			set varna_iso_node [$graph_nodes get 0]

			set varna_iso_obj [convert_graph_node $varna_iso_node $varna_iso_def_node]

			set phonoAttribVector [$varna_iso_obj get "phonoAttribVector"]
			set audio [$varna_iso_obj get "audio"]

			$varna_obj put "phonoAttribVector" $phonoAttribVector
			$varna_obj put "audio" $audio
		}
	}
	$varnas_list add $varna_obj
}
return $varnas_list


