package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set lemma_list [java::new ArrayList]
set object_type "Synset"
set error_status "Failed"

set get_node_response [getDataNode $language_id $synset_id]
return $get_node_response

