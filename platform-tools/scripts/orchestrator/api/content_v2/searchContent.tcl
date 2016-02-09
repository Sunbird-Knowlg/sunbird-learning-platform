package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_null [java::isnull $search]
if {$object_null == 1} {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_INVALID_SEARCH_REQUEST"
	$result_map put "message" "Invalid Search Request"
	$result_map put "responseCode" [java::new Integer 400]
	set response_list [create_error_response $result_map]
	return $response_list
} else {
	set invalidObjectType false
	set object_type_param [$search get "objectType"]
	set object_type_param_null [java::isnull $object_type_param]
	if {$object_type_param_null == 0} {
		set str_object_type_param [$object_type_param toString]
		if {$str_object_type_param != "Content"} {
			set invalidObjectType true
		}
	} 
	if {$invalidObjectType} {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_CONTENT_NOT_FOUND"
		$result_map put "message" "No content found"
		$result_map put "responseCode" [java::new Integer 404]
		set response_list [create_error_response $result_map]
		return $response_list
	} else {
		set object_type "Content"
		set check_null [java::isnull $search]
		if {$search == 1} {
			set $search [java::new HashMap]
		}

		$search put "objectType" $object_type
		$search put "nodeType" "DATA_NODE"

		set sort [$search get "sort"]
		set limit [$search get "limit"]
		$search put "sortBy" $sort
		$search put "resultSize" $limit

		$search remove "sort"
		$search remove "limit"

		set returnFields false
		set fieldList [$search get "fields"]
		set is_fieldList_null [java::isnull $fieldList]
		if {$is_fieldList_null == 0} {
			$search remove "fields"
			set returnFields true
		}

		set search_criteria [create_search_criteria $search]
		set graph_id "domain"
		set search_response [searchNodes $graph_id $search_criteria]
		set check_error [check_response_error $search_response]
		if {$check_error} {
			puts "Error response from searchNodes"
			return $search_response;
		} else {
			set graph_nodes [get_resp_value $search_response "node_list"]
			set resp_def_node [getDefinition $graph_id $object_type]
			set def_node [get_resp_value $resp_def_node "definition_node"]
			set obj_list [java::new ArrayList]
			java::for {Node graph_node} $graph_nodes {
				if {$returnFields} {
					set domain_obj [convert_graph_node $graph_node $def_node $fieldList]
				} else {
					set domain_obj [convert_graph_node $graph_node $def_node]
				}
				$obj_list add $domain_obj
			}
			set result_map [java::new HashMap]
			$result_map put "content" $obj_list
			set response_list [create_response $result_map]
			return $response_list
		}
	}
}
