package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_type "Content"
set graph_id "domain"
set resp_obj_list [java::new ArrayList]
set resp_content_nodes [getNodesByObjectType $graph_id $object_type]
set isRespContentNodesNotNull [isNotNull $resp_content_nodes]
if (isRespContentNodesNotNull) {
	set content_nodes [get_resp_value $resp_content_nodes "content"]
	java::for {Node content_node} $content_nodes {
		set metadata [java::prop $content_node "metadata"]
		set checkMetadata [isNotNull $metadata]
		if {$checkMetadata} {
			$metadata put "lastPublishedOn" [$metadata get "lastUpdatedOn"]
			$content_node put "metadata" $metadata
			set create_response [updateDataNode $graph_id $content_id $content_node]
			$resp_obj_list add $create_response	
		}
	}
}
return resp_obj_list