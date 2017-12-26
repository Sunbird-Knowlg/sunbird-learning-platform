package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set object_type "Content"
set graph_id "domain"
set map [java::new HashMap]
$map put "nodeType" "DATA_NODE"
$map put "objectType" $object_type

set contentTypes [java::new ArrayList]
$contentTypes add "Asset"
$map put "contentType" $contentTypes

set search_criteria [create_search_criteria $map]
set search_response [searchNodes $graph_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set graph_nodes [get_resp_value $search_response "node_list"]
	java::for {Node graph_node} $graph_nodes {
		set asset_id [java::prop $graph_node "identifier"]
		set metadata [java::prop $graph_node "metadata"]
		set license [$metadata get "license"]
		set license_null [java::isnull $license]
		set isCCBy 1
		if {$license_null == 0} {
			set license_str [java::new String [$license toString]]
			set isCCBy [$license_str equalsIgnoreCase "Creative Commons Attribution (CC BY)"]
		}
		if {$license_null == 1 || $isCCBy != 1} {
			$metadata put "license" "Creative Commons Attribution (CC BY)"
			puts "updating asset: $asset_id"
			set update_response [updateDataNode $graph_id $asset_id $graph_node]
			set check_error [check_response_error $update_response]
		    if {$check_error} {
		        set messages [get_resp_value $update_response "messages"]
		        java::for {String msg} $messages {
		            puts "$asset_id - $msg"
		        }
		    }
		}
	}
	puts "update complete"
}
return "Updation Complete"