package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node


set resp_nodes [getNodesByObjectType $graph_id $object_type]
set check_error [check_response_error $resp_nodes]

set null_value [java::null]

if {$check_error} {
	return $resp_nodes;
} else {
	set graph_nodes [get_resp_value $resp_nodes "node_list"]
	java::for {Node graph_node} $graph_nodes {
		set varna_id [java::prop $graph_node "identifier"]
		set metadataMap [java::prop $graph_node "metadata"]
		set obj_type [java::prop $graph_node "objectType"]
		
		set ipa_value [$metadataMap get "ipaSymbol"]
		
		if {[java::isnull $ipa_value] == 0} {
			$metadataMap put "isoSymbol" $ipa_value
			$metadataMap put "ipaSymbol" $null_value
			set update_response [updateDataNode $graph_id $varna_id $graph_node]
			set check_update_error [check_response_error $update_response]
			if {$check_update_error} {
				return $update_response;
			} else {
				puts "Successful response for updating status from Live to Draft for the varna $varna_id"
			}		
		}
	}
	return "Completed"
}
