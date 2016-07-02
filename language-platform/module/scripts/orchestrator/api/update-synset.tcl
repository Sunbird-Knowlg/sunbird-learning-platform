package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set lemma_list [java::new ArrayList]
set object_type "Synset"
set error_status "Failed"

set resp_def_node [getDefinition $language_id $object_type]
set def_node [get_resp_value $resp_def_node "definition_node"]
$synset put "objectType" $object_type
$synset put "identifier" $synset_id
set synset_obj [convert_to_graph_node $synset $def_node]
set create_response [updateDataNode $language_id $synset_id $synset_obj]
return $create_response

