package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util Date
java::import -package com.ilimi.graph.dac.model Node


proc proc_searchNodes {startPosition resultSize} {
	set object_type "Content"
	set graph_id "domain"
	set map [java::new HashMap]
	$map put "nodeType" "DATA_NODE"
	$map put "objectType" $object_type
	$map put "startPosition" [java::new Integer $startPosition]
	$map put "resultSize" [java::new Integer $resultSize]

	set search_criteria [create_search_criteria $map]
	set search_response [searchNodes $graph_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		set params [java::prop $search_response "params"]
		if {[java::isnull $params] == 0} {
			set msg [java::prop $params "errmsg"]
		}
		set graph_nodes [java::null]
		return $graph_nodes
	} else {
		set graph_nodes [get_resp_value $search_response "node_list"]
		return $graph_nodes
	}
}

set graph_id "domain"
set count 1
set startPosistion 0
set resultSize 5000

while {$count > 0} {
    set nodeList [proc_searchNodes $startPosistion $resultSize]
    set hasValue [java::isnull $nodeList]
    if {$hasValue == 1} {
    	set count 0
    	break
    } else {
    	set size [$nodeList size]
    	if {$size > 0} {
    		java::for {Node graph_node} $nodeList {
				set content_id [java::prop $graph_node "identifier"]
				set metadata [java::prop $graph_node "metadata"]
				set compatibilityLevel [$metadata get "compatibilityLevel"]
				set compatibilityLevel_null [java::isnull $compatibilityLevel]
				if {$compatibilityLevel_null == 1} {
					$metadata put "compatibilityLevel" [java::new Integer 1]
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
    	} else {
    		set count 0
    		break
    	}
    }
    set startPosistion [expr {$startPosistion+5000}]
}
return "Updation Complete"