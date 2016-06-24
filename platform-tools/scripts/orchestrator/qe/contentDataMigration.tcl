package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_type "Content"
set graph_id "domain"
set resp_obj_list [java::new ArrayList]
set resp_content_nodes [getNodesByObjectType $graph_id $object_type]
set isRespContentNodesNull [java::isnull $resp_content_nodes]
if {$isRespContentNodesNull == 0} {
	set content_nodes [get_resp_value $resp_content_nodes "node_list"]
	java::for {Node content_node} $content_nodes {
		set update 0
		set content_id [java::prop $content_node "identifier"]
		set metadata [java::prop $content_node "metadata"]
		set status [[$metadata get "status"] toString]
		set contentType [[$metadata get "contentType"] toString]
		if {$status == "Live"} {
			$metadata put "lastPublishedOn" [$metadata get "lastUpdatedOn"]
			set update 1
		}
		set size [$metadata get "size"]
		set isSizeNull [java::isnull $size]
		if {$isSizeNull == 0} {
			set size_instance [java::instanceof $size String]
			if {$size_instance == 1} {
				java::try {
					set string_size [$size toString]
					set numeric_size [java::new Double $string_size]
					$metadata put "size" $numeric_size
					set update 1
				} catch {Exception err} {
			    	puts "Error: $err"
				}
			}
		}
		if {$update == 1} {
			java::prop $content_node "metadata" $metadata
			puts "update content $content_id"
			set update_response [updateDataNode $graph_id $content_id $content_node]
			set check_error [check_response_error $update_response]
			if {$check_error} {
				puts "Failed to update $content_id $update_response"
				set messages [get_resp_value $update_response "messages"]
				java::for {String msg} $messages {
					puts "$msg"
				}
			}
		}
	}
}
return "Migration Complete"