package require java
java::import -package java.util HashMap Map
java::import -package org.ekstep.search.router SearchRequestRouterPool
java::import -package com.ilimi.common.router RequestRouterPool

set baseActorSystem [java::call RequestRouterPool getActorSystem]
set searchActorSystem [java::call SearchRequestRouterPool init $baseActorSystem]

set result_map [java::new HashMap]
$result_map put "status" "OK"
set response_list [create_response $result_map]
return $response_list
