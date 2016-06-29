package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set graph_id "domain"

set map [java::new HashMap]
$map put "nodeType" "DATA_NODE"
$map put "objectType" "Content"

set filter_list [java::new ArrayList]

set filter1 [java::new HashMap]
$filter1 put "property" "contentType"
$filter1 put "operator" "="
$filter1 put "value" "Asset"
$filter_list add $filter1

set filter2 [java::new HashMap]
$filter2 put "property" "mimeType"
$filter2 put "operator" "="
$filter2 put "value" "application/octet-stream"
$filter_list add $filter2

set filter3 [java::new HashMap]
$filter3 put "property" "downloadUrl"
$filter3 put "operator" "endsWith"
$filter3 put "value" $file_suffix
$filter_list add $filter3

$map put "filters" $filter_list
set search_criteria [create_search_criteria $map]
set search_response [searchNodes $graph_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set graph_nodes [get_resp_value $search_response "node_list"]
	set size [$graph_nodes size]
	java::for {Node graph_node} $graph_nodes {
		set object_id [java::prop $graph_node "identifier"]
		set metadata [java::prop $graph_node "metadata"]
		set isMetadataNull [java::isnull $metadata]
		if {$isMetadataNull == 1} {
			set metadata [java::new HashMap]
		}
		$metadata put "mediaType" $mediaType
		java::prop $graph_node "metadata" $metadata
		set update_response [updateDataNode $graph_id $object_id $graph_node]
		set check_error [check_response_error $update_response]
		if {$check_error} {

		}
	}
	set result_map [java::new HashMap]
	$result_map put "status" "ok"
	set response_list [create_response $result_map]
	return $response_list
}


