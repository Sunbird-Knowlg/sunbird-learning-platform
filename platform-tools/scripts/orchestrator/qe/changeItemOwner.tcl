package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

proc proc_isNumeric {value} {
	set exist false
	set is_null [java::isnull $value]
	if {$is_null == 1} {
    	return $exist
    } else {
    	java::try {
			set string [java::call Double parseDouble [$value toString]]
			set exist true
		} catch {Exception err} {
	    	set exist false
		}
		return $exist
    }
}

set object_type "AssessmentItem"
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
		set item_id [java::prop $graph_node "identifier"]
		set metadata [java::prop $graph_node "metadata"]
		set owner [$metadata get "owner"]
		set portalOwner [$metadata get "portalOwner"]
		set owner_null [java::isnull $owner]
		set portalOwner_null [java::isnull $portalOwner]
		if {$owner_null == 0 && $portalOwner_null == 1} {
			set isNumeric [proc_isNumeric $owner]
			if {$isNumeric} {
				set str_owner [$owner toString]
				puts "updating item: $item_id | owner - $str_owner"
				$metadata put "portalOwner" $owner
				set update_response [updateDataNode $graph_id $item_id $graph_node]
				set check_error [check_response_error $update_response]
			    if {$check_error} {
			        set messages [get_resp_value $update_response "messages"]
			        java::for {String msg} $messages {
			            puts "$item_id - $msg"
			        }
			    }
			}
		}
	}
}
return "Updation Complete"