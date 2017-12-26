package require java
java::import -package java.util HashMap Map
java::import -package java.util ArrayList List
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

set graph_id "domain"
set object_type "AssessmentItem"
set search [java::new HashMap]
$search put "objectType" $object_type
$search put "nodeType" "DATA_NODE"

set search_criteria [create_search_criteria $search]
set search_response [searchNodes $graph_id $search_criteria]
set graph_nodes [get_resp_value $search_response "node_list"]
set item_list [java::new ArrayList]
java::for {Node graph_node} $graph_nodes {
	set itemMap [java::new HashMap]
	set itemId [java::prop $graph_node "identifier"]
	$itemMap put "identifier" $itemId
	set metadata [java::prop $graph_node "metadata"]
	$itemMap put "type" [$metadata get "type"]
	$itemMap put "question" [$metadata get "question"]
	$itemMap put "title" [$metadata get "title"]
	$itemMap put "qlevel" [$metadata get "qlevel"]
	set inRelations [java::prop $graph_node "inRelations"]
	set hasInRelations [isNotEmpty $inRelations]
	set count 0
	if {$hasInRelations} {
		java::for {Relation relation} $inRelations {
			if {[java::prop $relation "startNodeObjectType"] == "ItemSet" && [java::prop $relation "relationType"] == "hasMember"} {
				set count [expr $count + 1]
			}
		}
	}
	$itemMap put "contentCount" $count
	$item_list add $itemMap
}
set size [$item_list size]
puts "size is $size"

set result_map [java::new HashMap]
$result_map put "items" $item_list
set response_list [create_response $result_map]
set response_csv [convert_response_to_csv $response_list "items"]
return $response_csv

