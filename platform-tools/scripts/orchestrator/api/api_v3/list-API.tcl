package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_type "Api"
set graph_id "domain"

set get_node_response [getAllObjects $graph_id $object_type]
set api_object [get_resp_value $get_node_response "object_list"]

set result_map [java::new HashMap]
$result_map put "apis" $api_object
set api_response [create_response $result_map]
return $api_response
