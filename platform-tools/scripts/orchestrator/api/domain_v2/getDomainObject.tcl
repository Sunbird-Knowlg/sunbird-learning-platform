package require java
java::import -package java.util HashMap Map

set object_type_res [getDomainObjectType $type]
set check_obj_type_error [check_response_error $object_type_res]
if {$check_obj_type_error} {
	return $object_type_res
} else {
	set object_type [get_resp_value $object_type_res "result"]
	set graph_id "domain"
	set resp_get_node [getDataNode $graph_id $object_id]
	set check_error [check_response_error $resp_get_node]
	if {$check_error} {
		return $resp_get_node;
	} else {
		set returnFields false
		set is_fieldList_null [java::isnull $fields]
		if {$is_fieldList_null == 0} {
			set returnFields true
		}

		set graph_node [get_resp_value $resp_get_node "node"]
		set resp_def_node [getDefinition $graph_id $object_type]
		set def_node [get_resp_value $resp_def_node "definition_node"]
		if {$returnFields} {
			set resp_object [convert_graph_node $graph_node $def_node $fields]
		} else {
			set resp_object [convert_graph_node $graph_node $def_node]
		}
		set result_map [java::new HashMap]
		$result_map put $object_type $resp_object
		set response_list [create_response $result_map]
		return $response_list
	}
}