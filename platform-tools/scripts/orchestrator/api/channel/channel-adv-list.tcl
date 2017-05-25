package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_type "Channel"
set graph_id "domain"

set get_node_response [getAllObjects $graph_id $object_type]
set channel_object [get_resp_value $get_node_response "object_list"]

set result_map [java::new HashMap]
$result_map put "label" $object_type
$result_map put "value" "channel"
$result_map put "language" "en"
$result_map put "description" "List of partner channels"
$result_map put "channels" $channel_object
set channel_response [create_response $result_map]
return $channel_response
