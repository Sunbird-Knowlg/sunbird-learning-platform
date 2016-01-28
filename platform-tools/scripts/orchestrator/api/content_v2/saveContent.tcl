package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_type "Content"
set graph_id "domain"
set resp_def_node [getDefinition $graph_id $object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]
$content put "objectType" $object_type
set domain_obj [convert_to_graph_node $content $def_node]
set create_response [createDataNode $graph_id $domain_obj]
return $create_response