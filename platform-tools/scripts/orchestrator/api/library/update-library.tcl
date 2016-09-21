package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

set object_type "Library"
set graph_id "domain"

set update_response [updateObject $graph_id $library $object_type $library_id]
return $update_response

