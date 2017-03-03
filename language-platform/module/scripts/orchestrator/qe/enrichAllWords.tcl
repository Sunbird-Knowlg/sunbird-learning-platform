package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation


proc isNotEmpty {list} {
	set exist false
	set isEmpty [java::isnull $list]
	if {$isEmpty == 0} {
		set listSize [$list size] 
		if {$listSize > 0} {
			set exist true
		}
	}
	return $exist
}

proc getWords { language_id startPosition resultSize } {

	set object_type "Word"
	set map [java::new HashMap]
	$map put "objectType" $object_type
	$map put "nodeType" "DATA_NODE"
	$map put "startPosition" [java::new Integer $startPosition]
	$map put "resultSize" [java::new Integer $resultSize]

	set search_criteria [create_search_criteria $map]
	set synsets [java::new ArrayList]

	set search_response [searchNodes $language_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
	} else {
		set graph_nodes [get_resp_value $search_response "node_list"]
		set synsets [java::cast List $graph_nodes]
	}

	return $synsets

}

set startPosition 0
set resultSize 1000
set continue true

while {$continue} {
	
	set words [getWords $language_id $startPosition $resultSize]
	set hasWords [isNotEmpty $words]
	set wordIds [java::new ArrayList]
	if {$hasWords} {
		java::for {Node word} $words {
			set wordIdentifier [java::prop $word "identifier"]
			$wordIds add $wordIdentifier
		}
		set enrichWordResponse [enrichWords $language_id $wordIds]
	} else {
		set continue false
	}
	set startPosition [expr $startPosition + $resultSize]
}
