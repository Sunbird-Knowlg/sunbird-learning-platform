package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

proc proc_isEmpty {value} {
	set exist false
	java::try {
		set hasValue [java::isnull $value]
		if {$hasValue == 1} {
			set exist true
		} else {
			set strValue [$value toString]
			set newStrValue [java::new String $strValue] 
			set strLength [$newStrValue length]
			if {$strLength == 0} {
				set exist true
			}
		}
	} catch {Exception err} {
    	set exist true
	}
	return $exist
}

set object_type "Content"
set graph_id "domain"
set map [java::new HashMap]
$map put "nodeType" "DATA_NODE"
$map put "objectType" $object_type

set contentTypes [java::new ArrayList]
$contentTypes add "Story"
$contentTypes add "Worksheet"
$contentTypes add "Collection"
$map put "contentType" $contentTypes

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
		set body [$metadata get "body"]
		set bodyEmpty [proc_isEmpty $body]
		if {!$bodyEmpty} {
			puts "cleaning content body: $content_id"
			set bodyResponse [updateContentBody $content_id $body]
			$metadata put "body" [java::null]
			set update_response [updateDataNode $graph_id $content_id $graph_node]
			set check_error [check_response_error $update_response]
			if {$check_error} {
				set messages [get_resp_value $update_response "messages"]
				java::for {String msg} $messages {
					puts "$msg"
				}
			}
		}
	}
	puts "update complete"
}
return "Updation Complete"