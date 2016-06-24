package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node
java::import -package com.ilimi.common.dto NodeDTO

set varna_object_type "Varna"
set varna_iso_object_type "Varna_ISO"
set varna_iso_graph_id "language"
set consonantString [java::new String "Consonant"]
set object_null [java::isnull $syllable]
if {$object_null == 1} {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_INVALID_REQUEST"
	$result_map put "message" "syllable IS MANDATORY"
	$result_map put "responseCode" [java::new Integer 400]
	set response_list [create_error_response $result_map]
	return $response_list
} 

set language_id [get_language_graph_id $syllable]

set charArray [$syllable toCharArray]

set unicode_list [java::new ArrayList]
java::for {char ch} $charArray {
	set charInt [scan $ch %c]
	set charInteger [java::new Integer $charInt]
	set object [java::cast Object $charInteger]
	set object_list [java::new {Object[]} {1}]
	$object_list set 0 $object
	set charUnicode [java::call String format "%04x" $object_list]
	set charUnicodeString [java::new String $charUnicode]
	set charUnicodeUpper [$charUnicodeString toUpperCase]
	$unicode_list add $charUnicodeUpper
}
set consonant_list [java::new ArrayList]
java::for {String unicode} $unicode_list {
	set searchProperty [java::new HashMap]
	$searchProperty put "unicode" $unicode
	set property [create_search_property $searchProperty]

	set search_response [getNodesByProperty $language_id $property]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		puts "Error response from searchNodes"
	} else {
		set graph_nodes [get_resp_value $search_response "node_list"]
		set varna_node [$graph_nodes get 0]

		set resp_def_node [getDefinition $language_id $varna_object_type]
		set def_node [get_resp_value $resp_def_node "definition_node"]
		set varna_obj [convert_graph_node $varna_node $def_node]

		set varnaType [$varna_obj get "type"]
		set varnaTypeTemp [$varnaType toString]
		set varnaTypeString [java::new String $varnaTypeTemp]
		puts $varnaTypeTemp
		set isConsonant [$varnaTypeString equalsIgnoreCase $consonantString]
		if {$isConsonant == 1} {
			$consonant_list add $varna_obj
		}	
	}
}
return $consonant_list




