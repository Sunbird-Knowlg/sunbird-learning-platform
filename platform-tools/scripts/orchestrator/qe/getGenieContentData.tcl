package require java
java::import -package java.util HashMap Map
java::import -package java.util ArrayList List
java::import -package com.ilimi.graph.dac.model Node Relation

set graph_id "domain"
set object_type "Content"
set search [java::new HashMap]
$search put "objectType" $object_type
$search put "nodeType" "DATA_NODE"
$search put "status" "Live"

set contentTypes [java::new ArrayList]
$contentTypes add "Story"
$contentTypes add "Worksheet"
$contentTypes add "Game"
$contentTypes add "Collection"
$search put "contentType" $contentTypes

set search_criteria [create_search_criteria $search]
set search_response [searchNodes $graph_id $search_criteria]
set graph_nodes [get_resp_value $search_response "node_list"]
set item_list [java::new ArrayList]
java::for {Node graph_node} $graph_nodes {
	set itemMap [java::new HashMap]
	set itemId [java::prop $graph_node "identifier"]
	$itemMap put "identifier" $itemId
	set metadata [java::prop $graph_node "metadata"]
	$itemMap put "name" [$metadata get "name"]
	$itemMap put "contentType" [$metadata get "contentType"]
	$itemMap put "size" [$metadata get "size"]
	$itemMap put "language" [$metadata get "language"]
	$item_list add $itemMap
}

set result_map [java::new HashMap]
$result_map put "content" $item_list
return $result_map

