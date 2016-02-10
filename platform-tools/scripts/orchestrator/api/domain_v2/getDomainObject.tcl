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
		set node_object_type [java::prop $graph_node "objectType"]
		set str_object_type [$object_type toString]
		if {$node_object_type == $str_object_type} {
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
		} else {
			set result_map [java::new HashMap]
			$result_map put "code" "ERR_NODE_NOT_FOUND"
			$result_map put "message" "$str_object_type $object_id not found"
			$result_map put "responseCode" [java::new Integer 404]
			set response_list [create_error_response $result_map]
			return $response_list
		}
	}
}