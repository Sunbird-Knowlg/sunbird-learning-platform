package require java
java::import -package java.util HashMap Map

if {$type == "dimensions"} {
	return "Dimension"
} elseif {$type == "concepts"} {
	return "Concept"
} elseif {$type == "misconceptions"} {
	return "Misconception"
} elseif {$type == "methods"} {
	return "Method"
} else {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_DOMAIN_INVALID_OBJECT_TYPE"
	$result_map put "message" "Invalid Object Type"
	$result_map put "responseCode" [java::new Integer 400]
	set response_list [create_error_response $result_map]
	return $response_list
}