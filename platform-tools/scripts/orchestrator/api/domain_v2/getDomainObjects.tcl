package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Filter
java::import -package com.ilimi.graph.dac.model Node


set is_sortBy_null [java::isnull $sort]
set is_resultSize_null [java::isnull $limit]
set object_type_res [getDomainObjectType $type]
set check_obj_type_error [check_response_error $object_type_res]
if {$check_obj_type_error} {
	return $object_type_res
} else {
	set object_type [get_resp_value $object_type_res "result"]
	set map [java::new HashMap]
	$map put "subject" $domain_id
	$map put "objectType" $object_type
	if {$is_sortBy_null == 0} {
		$map put "sortBy" $sort
	}
	if {$is_resultSize_null == 0} {
		$map put "resultSize" $limit
	}
	$map put "order" $order

	set search_criteria [create_search_criteria $map]
	set graph_id "domain"
	set search_response [searchNodes $graph_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		puts "Error response from searchNodes"
		return $search_response;
	} else {
		set returnFields false
		set is_fieldList_null [java::isnull $fields]
		if {$is_fieldList_null == 0} {
			set returnFields true
		}

		set graph_nodes [get_resp_value $search_response "node_list"]
		set resp_def_node [getDefinition $graph_id $object_type]
		set def_node [get_resp_value $resp_def_node "definition_node"]
		set obj_list [java::new ArrayList]
		java::for {Node graph_node} $graph_nodes {
			if {$returnFields} {
				set domain_obj [convert_graph_node $graph_node $def_node $fields]
			} else {
				set domain_obj [convert_graph_node $graph_node $def_node]
			}
			$obj_list add $domain_obj
		}
		set result_map [java::new HashMap]
		$result_map put $type $obj_list
		set response_list [create_response $result_map]
		return $response_list
	}
}