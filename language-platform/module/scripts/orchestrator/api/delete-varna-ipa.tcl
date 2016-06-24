package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set varna_ipa_Obj_type "Varna_IPA"

set resp_nodes [getNodesByObjectType $graph_id $varna_ipa_Obj_type]
set check_error [check_response_error $resp_nodes]

if {$check_error} {
	return $resp_nodes;
} else {
	set graph_nodes [get_resp_value $resp_nodes "node_list"]
	java::for {Node graph_node} $graph_nodes {
		set varna_id [java::prop $graph_node "identifier"]
		set delete_response [deleteDataNode $graph_id $varna_id]
		set check_delete_error [check_response_error $delete_response]
		if {$check_delete_error} {
			return $delete_response;
		}
	}
	return "Completed"
}
