package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set object_type "Reference"
set graph_id "domain"

set get_node_response [getObject $graph_id $object_type $reference_id]
set reference_object [get_resp_value $get_node_response "object"]

set result_map [java::new HashMap]
$result_map put "reference" $reference_object
set api_response [create_response $result_map]
return $api_response

