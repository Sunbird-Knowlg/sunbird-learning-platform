package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_type "TraversalRule"
set error_status "Failed"

set resp_def_node [getDefinition $language_id $object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]
$rule put "objectType" $object_type
$rule put "identifier" $rule_id
set rule_obj [convert_to_graph_node $rule $def_node]
set create_response [updateDataNode $language_id $rule_id $rule_obj]
return $create_response

