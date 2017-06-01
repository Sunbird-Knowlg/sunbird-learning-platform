package require java
java::import -package java.util HashMap Map Date
java::import -package java.util ArrayList List
java::import -package java.util HashSet Set
java::import -package java.util Arrays
java::import -package com.ilimi.graph.dac.model Node
java::import -package com.ilimi.graph.common DateUtils
java::import -package com.ilimi.graph.dac.model Relation
java::import -package java.util Arrays

set graph_id "domain"
set object_type "Content"
set image_object_type "ContentImage" 

proc isNotEmpty {list} {
	set exist false
	set isListNull [java::isnull $list]
	if {$isListNull == 0} {
		set listSize [$list size]
		if {$listSize > 0} {
			set exist true
		}
	}
	return $exist
}

proc proc_isNotNull {value} {
	set exist false
	java::try {
		set hasValue [java::isnull $value]
		if {$hasValue == 0} {
			set exist true
		}
	} catch {Exception err} {
    	set exist false
	}
	return $exist
}

proc proc_getScreenshots {graph_node} {
        set urlSet [java::new HashSet]
	set result_map [java::new Relation]
        set node_metadata [java::prop $graph_node "metadata"]
		set outRelations [java::prop $graph_node "outRelations"]
		java::for {Relation object} $outRelations {
		set resultMap $object
		set relationType [java::prop $resultMap "relationType"]
			if {$relationType == "associatedTo"} {
				set endNodeObjectType [java::prop $resultMap "endNodeObjectType"]
				if {$endNodeObjectType == "Content"} {
			 		set endNodeMetadata [java::prop $resultMap "endNodeMetadata"]
					set endNodeContentType [$endNodeMetadata get "contentType"]
					set contentType [$endNodeContentType toString]
					if {$contentType == "Asset"} {
					     set artifactUrl [$endNodeMetadata get "artifactUrl"]
					     set url [java::cast String $artifactUrl]
					     set urlNotNull [proc_isNotNull $url]
		 			     if {$urlNotNull} { 
						$urlSet add $url
				             }
				       }
				} 
		         }
		}
		set screenshots [$node_metadata get "screenshots"]
		set arr_instance [java::instanceof $screenshots {String[]}]
		if {$arr_instance == 1} {
			set existingAsset [java::cast {String[]} $screenshots]
			set existingData [java::call Arrays asList $existingAsset]
		                
		}
		set screenShotNotNull [proc_isNotNull $existingData]
		if {$screenShotNotNull} {
	   	 	$urlSet addAll $existingData
		}
		set urls [java::new ArrayList $urlSet]
		$node_metadata put "screenshots" $urls
		return $graph_node
      
}

set resp_get_node [getDataNode $graph_id $content_id]
set check_error [check_response_error $resp_get_node]
if {$check_error} {
	return $resp_get_node;
} else {
        	
		set graph_node [get_resp_value $resp_get_node "node"]
		set node_object_type [java::prop $graph_node "objectType"]
              	set node_metadata [java::prop $graph_node "metadata"]
		set outRelations [java::prop $graph_node "outRelations"]
      		set relation_val_null [java::isnull $outRelations]
                set relation_isNotEmpty [isNotEmpty $outRelations]
      		if {$relation_val_null == 1} {
        		set result_map [java::new HashMap]
			$result_map put "code" "ERR_CONTENT"
			$result_map put "message" "Content has no relations"
			$result_map put "responseCode" [java::new Integer 400]
			set response_list [create_error_response $result_map]
			return $response_list
        	}
		if {$relation_isNotEmpty && $node_object_type == $object_type} {
				proc_getScreenshots $graph_node
				set create_response [updateDataNode $graph_id $content_id $graph_node]
		       		set content_image_id ${content_id}.img
		    		set resp_get_node [getDataNode $graph_id $content_image_id]
		    		set check_error [check_response_error $resp_get_node]
				if {$check_error} {
					return $create_response
				} else {
					set image_node [get_resp_value $resp_get_node "node"]
					proc_getScreenshots $image_node
					set create_image_response [updateDataNode $graph_id $content_image_id $image_node]         
		    		}
                    	return $create_response
   		 	
		}
      
}
    
