package require java
java::import -package java.util HashMap Map
java::import -package java.util ArrayList List
java::import -package org.ekstep.graph.dac.model Node
java::import -package org.ekstep.language.cache VarnaCache


set cache [java::call VarnaCache getInstance]
$cache loadVarnas $language_id