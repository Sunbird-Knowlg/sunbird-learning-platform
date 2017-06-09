package require java
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node
java::import -package java.util ArrayList List

set graph_id "domain"
set object_type "Content"
set original_content_id $content_id
set content_image_object_type "ContentImage"
set resp_get_node [getDataNode $graph_id $content_id]
set check_error [check_response_error $resp_get_node]
if {$check_error} {
    return $resp_get_node
} else {
    set isImageObjectCreationNeeded 0
    set imageObjectExists 0
    set graph_node [get_resp_value $resp_get_node "node"]
    set node_object_type [java::prop $graph_node "objectType"]
    set metadata [java::prop $graph_node "metadata"]
    if {$node_object_type == $object_type} {
        set node_metadata [java::prop $graph_node "metadata"]
        set status_val [$node_metadata get "status"]
        set status_val_str [java::new String [$status_val toString]]
        set isFlaggedstate [$status_val_str equalsIgnoreCase "Flagged"]
        if {$isFlaggedstate == 1} {
           set content_image_id ${content_id}.img
           set get_node_response [getDataNode $graph_id $content_image_id]
           set get_node_response_error [check_response_error $get_node_response]
           if {$get_node_response_error} {
                    set isImageObjectCreationNeeded 1
            } else {
                    set imageObjectExists 1
            }
        } else {
            set result_map [java::new HashMap]
            $result_map put "code" "ERR_CONTENT_NOT_FLAGGED"
            $result_map put "message" "Content $content_id is not flagged to accept"
            $result_map put "responseCode" [java::new Integer 400]
            set response_list [create_error_response $result_map]
            return $response_list
        }
        if {$isImageObjectCreationNeeded == 1} {
            java::prop $graph_node "identifier" $content_image_id
            java::prop $graph_node "objectType" $content_image_object_type
            
            if {$isFlaggedstate == 1} {
                $metadata put "status" "FlagDraft"
            }
            set create_response [createDataNode $graph_id $graph_node]
            set check_error [check_response_error $create_response]
            if {!$check_error} {
                set externalPropFields [java::new ArrayList]
                $externalPropFields add "body"
                $externalPropFields add "oldBody"
                $externalPropFields add "stageIcons"
                set bodyResponse [getContentProperties $content_id $externalPropFields]
                set check_error [check_response_error $bodyResponse]
                if {!$check_error} {
                    set extValues [get_resp_value $bodyResponse "values"]
                    set is_extValues_null [java::isnull $extValues]
                    if {$is_extValues_null == 0} {
                        set extValuesMap [java::cast Map $extValues]
                        set bodyResponse [updateContentProperties $content_image_id $extValuesMap]
                    }
                }
               $create_response put "node_id" $original_content_id
            }
            return $create_response
            
        } elseif {$imageObjectExists == 1} {
			set content_image_id ${content_id}.img
           	set get_node_response [getDataNode $graph_id $content_image_id]
            set image_node [get_resp_value $get_node_response "node"]
            set image_metadata [java::prop $image_node "metadata"]
            $image_metadata put "status" "FlagDraft"
            set create_response [updateDataNode $graph_id $content_image_id $image_node]
            set check_error [check_response_error $create_response]
            if {$check_error} {
                return $create_response
            } else {
                $create_response put "node_id" $original_content_id
            }
            return $create_response    
        }
    } else {
        set result_map [java::new HashMap] 
        $result_map put "code" "ERR_NODE_NOT_FOUND"
        $result_map put "message" "$object_type $content_id not found"
        $result_map put "responseCode" [java::new Integer 404]
        set response_list [create_error_response $result_map]
        return $response_list
    }
}

