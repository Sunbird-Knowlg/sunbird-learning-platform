package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node


proc isNotNull {value} {
	set exist false
	java::try {
		set hasValue [java::isnull $value]
		if {$hasValue == 0} {
			set exist true
		}
	} catch {Exception err} {
    	set exist false
	}
	return $exist
}

set wordMap [java::new HashMap]
set isNodeNotNull [isNotNull $node]
if {$isNodeNotNull} {
	set metadata [java::prop $node "metadata"]
	set checkMetadata [isNotNull $metadata]
	if {$checkMetadata} {
		$wordMap putAll $metadata
	}
}
return $wordMap

