package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set object_type "Varna_ISO"
set error_status "Failed"
set language_id "language"

set resp_def_node [getDefinition $language_id $object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]
$iso put "objectType" $object_type
set identifer [$iso get "isoSymbol"]
$iso put "identifier" $identifer
set varnaISO_obj [convert_to_graph_node $iso $def_node]
set create_response [createDataNode $language_id $varnaISO_obj]
return $create_response

