package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set object_type "Varna"
set error_status "Failed"

set resp_def_node [getDefinition $language_id $object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]
$varna put "objectType" $object_type
set identifer [$varna get "varna"]
$varna put "identifier" $identifer
set varna_obj [convert_to_graph_node $varna $def_node]
set create_response [createDataNode $language_id $varna_obj]
return $create_response

