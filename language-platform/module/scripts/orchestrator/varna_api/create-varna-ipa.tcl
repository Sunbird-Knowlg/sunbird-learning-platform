package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_type "Varna_IPA"
set error_status "Failed"
set language_id "language"

set resp_def_node [getDefinition $language_id $object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]
$ipa put "objectType" $object_type
set identifer [$ipa get "ipaSymbol"]
$ipa put "identifier" $identifer
set varnaIPA_obj [convert_to_graph_node $ipa $def_node]
set create_response [createDataNode $language_id $varnaIPA_obj]
return $create_response

