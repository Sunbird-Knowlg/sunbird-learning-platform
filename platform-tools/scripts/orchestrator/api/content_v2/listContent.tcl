package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node RelationFilter

proc proc_createRelationFilter {relationName direction} {
	set filter [java::new RelationFilter $relationName]
	$filter setFromDepth [java::new Integer 0]
	$filter setToDepth [java::new Integer 0]
	$filter setToDepth [java::new Integer 0]
	$filter setDirection $direction
	return $filter
}

proc proc_setRelationCriteria {concepts_list objectType filters} {
	set relation_query [java::new HashMap]
	set concepts_list_null [java::isnull $concepts_list]
	if {$concepts_list_null == 0} {
		set is_list [java::instanceof $concepts_list List]
		if {$is_list == 1} {
			set concepts_list_obj [java::cast List $concepts_list]
			set concepts_list_size [$concepts_list_obj size]
			if {$concepts_list_size > 0} {
				$relation_query put "filters" $filters
				$relation_query put "objectType" $objectType
				set concept_ids [java::new ArrayList]
				java::for {String concept_id} $concepts_list_obj {
					$concept_ids add $concept_id
				}
				$relation_query put "identifiers" $concept_ids
			}
		}
	}
	return $relation_query
}

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
		set status_val [$search get "status"]
		set is_status_null [java::isnull $status_val]
		if {$is_status_null == 1} {
			$search put "status" "Live"
		}

		set filter_list [java::new ArrayList]
		set filter [java::new HashMap]
		$filter put "property" "contentType"
		$filter put "operator" "!="
		$filter put "value" "Asset"
		$filter_list add $filter

		set filter2 [java::new HashMap]
		$filter2 put "property" "contentType"
		$filter2 put "operator" "!="
		$filter2 put "value" "Template"
		$filter_list add $filter2
		$search put "filters" $filter_list

		set relations_list [java::new ArrayList]
		set concepts_list [$search get "concepts"]
		set concepts_list_null [java::isnull $concepts_list]
		if {$concepts_list_null == 0} {
			set is_list [java::instanceof $concepts_list List]
			if {$is_list == 1} {
				set concepts_list_obj [java::cast List $concepts_list]
				set concepts_list_size [$concepts_list_obj size]
				if {$concepts_list_size > 0} {					
					set relation_query [java::new HashMap]
					$relation_query put "name" "associatedTo"
					$relation_query put "objectType" "Concept"
					set concept_ids [java::new ArrayList]
					java::for {String concept_id} $concepts_list_obj {
						$concept_ids add $concept_id
					}
					$relation_query put "identifiers" $concept_ids
					$relations_list add $relation_query
					$search remove "concepts"
				}
			}
		}

		set relation_names [java::new ArrayList]
		$relation_names add [proc_createRelationFilter "associatedTo" "OUT"]
		$relation_names add [proc_createRelationFilter "isParentOf" "IN"]

		set domains_list [$search get "domains"]
		set domain_query_map [proc_setRelationCriteria $domains_list "Domain" $relation_names]
		set domain_query_empty [$domain_query_map isEmpty]
		if {!$domain_query_empty} {
			$relations_list add $domain_query_map
			$search remove "domains"
		}

		set dimensions_list [$search get "dimensions"]
		set dimension_query_map [proc_setRelationCriteria $dimensions_list "Dimension" $relation_names]
		set dimension_query_empty [$dimension_query_map isEmpty]
		if {!$dimension_query_empty} {
			$relations_list add $dimension_query_map
			$search remove "dimensions"
		}

		set relationsListSize [$relations_list size] 
		if {$relationsListSize > 0} {
			$search put "relationCriteria" $relations_list
		}

		set sort [$search get "sort"]
		set limit [$search get "limit"]
		set limit_null [java::isnull $limit]
		if {$limit_null == 1} {
			set limit [java::new Integer 50]
		}
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
			set content_ttl [java::new Integer 0]
			set def_metadata [java::prop $def_node "metadata"]
			set def_metadata_null [java::isnull $def_metadata]
			if {$def_metadata_null != 1} {
				set ttl_val [$def_metadata get "ttl"]
				set ttl_val_null [java::isnull $ttl_val]
				if {$ttl_val_null != 1} {
					set content_ttl $ttl_val
				}
			}
			set result_map [java::new HashMap]
			$result_map put "ttl" $content_ttl
			$result_map put "content" $obj_list
			set response_list [create_response $result_map]
			return $response_list
		}
	}
}
