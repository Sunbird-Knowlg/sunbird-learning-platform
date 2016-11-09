package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util Date
java::import -package com.ilimi.graph.dac.model Node

set object_type "Content"
set graph_id "domain"
set map [java::new HashMap]
$map put "nodeType" "DATA_NODE"
$map put "objectType" $object_type

set search_criteria [create_search_criteria $map]
set search_response [searchNodes $graph_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set graph_nodes [get_resp_value $search_response "node_list"]
	java::for {Node graph_node} $graph_nodes {
		set content_id [java::prop $graph_node "identifier"]
		set metadata [java::prop $graph_node "metadata"]
		set versionKey [$metadata get "versionKey"]
		set versionKey_null [java::isnull $versionKey]
		if {$versionKey_null == 1} {
			set name [$metadata get "name"]
			$metadata put "name" $name
			set update_response [updateDataNode $graph_id $content_id $graph_node]
			set check_error [check_response_error $update_response]
		    if {$check_error} {
		        set messages [get_resp_value $update_response "messages"]
		        java::for {String msg} $messages {
		            puts "$content_id - $msg"
		        }
		    }
		}
	}
}
return "Updation Complete"