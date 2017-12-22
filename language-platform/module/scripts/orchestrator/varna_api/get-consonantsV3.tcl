package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

set varnas [getAllVarnas $language_id "Consonant"]
return $varnas