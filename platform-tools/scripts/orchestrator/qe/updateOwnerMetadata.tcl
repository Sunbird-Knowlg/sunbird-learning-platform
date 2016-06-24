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

proc proc_searchNodes {startPosition resultSize} {
	set object_type "Content"
	set graph_id "domain"
	set map [java::new HashMap]
	$map put "objectType" $object_type
	$map put "nodeType" "DATA_NODE"
	$map put "startPosition" [java::new Integer $startPosition]
	$map put "resultSize" [java::new Integer $resultSize]

	set search_criteria [create_search_criteria $map]
	set search_response [searchNodes $graph_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
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
    			set contentId [java::prop $graph_node "identifier"]
				set metadata [java::prop $graph_node "metadata"]
				set owner [$metadata get "owner"]
				set portalOwner [$metadata get "portalOwner"]
				set isNumeric [proc_isNumeric $owner]
				if {$isNumeric} {
					set isPortalOwnerNumeric [proc_isNumeric $portalOwner]
					if {!$isPortalOwnerNumeric} {
						$metadata put "portalOwner" $owner
						set update_response [updateDataNode $graph_id $contentId $graph_node]
					}
				}	
    		}
    	} else {
    		set count 0
    		break
    	}
    }
    puts "update complete for $startPosistion - $resultSize content"
    set startPosistion [expr {$startPosistion+5000}]
}
return "OK"