package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node


set resp_nodes [getNodesByObjectType $graph_id $object_type]
set check_error [check_response_error $resp_nodes]

set null_value [java::null]

if {$check_error} {
	puts "Error response from getNodesByObjectType"
	return $resp_nodes;
} else {
	set graph_nodes [get_resp_value $resp_nodes "node_list"]
	puts "Got list of graph nodes"
	java::for {Node graph_node} $graph_nodes {
		set varna_id [java::prop $graph_node "identifier"]
		set metadataMap [java::prop $graph_node "metadata"]
		set obj_type [java::prop $graph_node "objectType"]
		
		set ipa_value [$metadataMap get "ipaSymbol"]
		
		if {[java::isnull $ipa_value] == 0} {
			$metadataMap put "isoSymbol" $ipa_value
			$metadataMap put "ipaSymbol" $null_value
			puts "Varna"
			set update_response [updateDataNode $graph_id $varna_id $graph_node]
			if {$check_update_error} {
				puts "Error response from updateDataNode for the varna $varna_id"
				puts $varna_id
				return $update_response;
			} else {
				puts "Successful response for updating status from Live to Draft for the varna $varna_id"
			}		
		}
	}
	return "Completed"
}
