package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set object_type "Channel"
set graph_id "domain"

set get_node_response [getObject $graph_id $object_type $channel_id]
set channel_object [get_resp_value $get_node_response "object"]

set result_map [java::new HashMap]
$result_map put "channel" $channel_object
set channel_response [create_response $result_map]
return $channel_response
