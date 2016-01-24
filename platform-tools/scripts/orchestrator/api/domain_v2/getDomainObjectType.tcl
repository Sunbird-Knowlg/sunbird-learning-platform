package require java
if {$type == "dimensions"} {
	return "Dimension"
} elseif {$type == "concepts"} {
	return "Concept"
} elseif {$type == "misconceptions"} {
	return "Misconception"
} elseif {$type == "methods"} {
	return "Method"
} else {
	return "Domain"
}