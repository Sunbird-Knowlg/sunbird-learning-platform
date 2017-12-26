package require java
java::import -package java.util Arrays
java::import -package java.util ArrayList List 
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package org.ekstep.graph.dac.model Node
java::import -package org.ekstep.graph.model.node DefinitionDTO
java::import -package org.ekstep.graph.model.node MetadataDefinition

proc isNotEmpty {list} {
	set exist false
	set hasData [java::isnull $list]
	if {$hasData == 0} {
		set listSize [$list size] 
		if {$listSize > 0} {
			set exist true
		}
	}
	return $exist
}

set graph_id "language"
set object_type "GradeLevelComplexity"

set resp_def_node [getDefinition $graph_id $object_type]
set check_error [check_response_error $resp_def_node]
if {$check_error} {
	return $resp_def_node
} 
set def_node [get_resp_value $resp_def_node "definition_node"]
set properties [java::prop $def_node "properties"]

java::for {MetadataDefinition property} $properties {
	set propertyName [java::prop $property "propertyName"]
	if {$propertyName=="languageId"} {
		set validLanguages [java::prop $property "range"]
		if {[$validLanguages contains $language_id]} {
			break
		} else {
			set result_map [java::new HashMap]
			$result_map put "code" "ERR_CONTENT_INVALID_REQUEST"
			$result_map put "message" "Invalid language Id"
			$result_map put "responseCode" [java::new Integer 400]
			set response_list [create_error_response $result_map]
			return $response_list
		}
	}
}

set map [java::new HashMap]
$map put "nodeType" "DATA_NODE"
$map put "objectType" $object_type
$map put "gradeLevel" $gradeLevel
$map put "languageLevel" $languageLevel
$map put "languageId" $language_id
set search_criteria [create_search_criteria $map]
set search_response [searchNodes $graph_id $search_criteria]

set nodeExist false
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set graph_nodes [get_resp_value $search_response "node_list"]	
	set hasNode [isNotEmpty $graph_nodes]
	if {$hasNode} {
		set gradelevel_complexity_node [$graph_nodes get 0]	
		set nodeExist true
	} else {
		set nodeExist false
	}
}

set text_complexity_response [computeTextComplexity $language_id $text]
set check_error [check_response_error $text_complexity_response]
if {$check_error} {
	return $text_complexity_response
} 
set text_complexity [get_resp_value $text_complexity_response "text_complexity"]
set text_mean_complexity [$text_complexity get "meanComplexity"]

set gradelevel_complexity [java::new HashMap]
$gradelevel_complexity put "objectType" $object_type
$gradelevel_complexity put "gradeLevel" $gradeLevel
$gradelevel_complexity put "languageLevel" $languageLevel
$gradelevel_complexity put "languageId" $language_id
set source_list [java::new ArrayList]
$source_list add $source
set node_id ""

if {$nodeExist} {
	set gradelevel_complexity_node [java::cast Node $gradelevel_complexity_node]
	set metadata [java::prop $gradelevel_complexity_node "metadata"]
	set node_id [java::prop $gradelevel_complexity_node "identifier"]
	set sources [$metadata get "sources"]
	set arr_instance [java::instanceof $sources {String[]}]
	if {$arr_instance == 1} {
			set sources [java::cast {String[]} $sources]
			set sources [java::call Arrays asList $sources]
	}
	set sources [java::cast List $sources]
	set sourcesSet [java::new HashSet $sources] 
	$sourcesSet add $source
	set source_list [java::new ArrayList $sourcesSet]

	set averageComplexity [$metadata get "averageComplexity"]
	set averageComplexity [$averageComplexity toString]
	set text_mean_complexity [$text_mean_complexity toString]
	set totalComplexity [expr {$text_mean_complexity + $averageComplexity}]
	set totalComplexity [expr {$totalComplexity / 2}]
	set text_mean_complexity $totalComplexity
}

set text_mean_complexity [java::new Double $text_mean_complexity]
$gradelevel_complexity put "averageComplexity" $text_mean_complexity
$gradelevel_complexity put "sources" $source_list
set gradelevel_complexity_node [convert_to_graph_node $gradelevel_complexity $def_node]

#call validating Complexity range
set resp_validate [validateComplexityRange $language_id $gradelevel_complexity_node ]
set check_error [check_response_error $resp_validate]
if {$check_error} {
	return $resp_validate;
}

if {$nodeExist} {
	set response [updateDataNode $graph_id $node_id $gradelevel_complexity_node]		
} else {
	set response [createDataNode $graph_id $gradelevel_complexity_node]
}

set check_error [check_response_error $response]
if {$check_error} {
	return $response;
} 
set node_id [get_resp_value $response "node_id"]
set load_response [loadGradeLevelComplexity $language_id $node_id]

return $load_response;


