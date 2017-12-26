package require java
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node Relation

proc isNotEmpty {relations} {
	set exist false
	set hasRelations [java::isnull $relations]
	if {$hasRelations == 0} {
		set relationsSize [$relations size] 
		if {$relationsSize > 0} {
			set exist true
		}
	}
	return $exist
}

set object_type "Concept"
set graph_id "domain"
set resp_get_node [getDataNode $graph_id $concept_id]
set check_error [check_response_error $resp_get_node]
if {$check_error} {
	return $resp_get_node;
} else {
	set result_map [java::new HashMap]
	java::try {
		set graph_node [get_resp_value $resp_get_node "node"]
		set resp_object [java::new HashMap]
		set methods 0
		set misconceptions 0
		set inRelations [java::prop $graph_node "inRelations"]
		set hasInRelations [isNotEmpty $inRelations]
		if {$hasInRelations} {
			java::for {Relation relation} $inRelations {
				if {[java::prop $relation "relationType"] == "associatedTo"} {
					if {[java::prop $relation "startNodeObjectType"] == "Method"} {
						set methods [expr {$methods+1}]
					}
					if {[java::prop $relation "startNodeObjectType"] == "Misconception"} {
						set misconceptions [expr {$misconceptions+1}]
					}
				}
			}
		}
		$resp_object put "misconceptions" $misconceptions
		$resp_object put "methods" $methods
		$result_map put "concept" $resp_object
	} catch {Exception err} {
    	$result_map put "error" [$err getMessage]
	}
	set response_list [create_response $result_map]
	return $response_list
}