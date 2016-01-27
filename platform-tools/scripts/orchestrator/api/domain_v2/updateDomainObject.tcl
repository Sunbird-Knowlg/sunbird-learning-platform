package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_type_res [getDomainObjectType $type]
set object_type [get_resp_value $object_type_res "result"]
set graph_id "domain"
set resp_def_node [getDefinition $graph_id $object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]
$object put "objectType" $object_type
$object put "identifier" $object_id
$object put "subject" $domain_id
set domain_obj [convert_to_graph_node $object $def_node]
set create_response [updateDataNode $graph_id $object_id $domain_obj]
return $create_response