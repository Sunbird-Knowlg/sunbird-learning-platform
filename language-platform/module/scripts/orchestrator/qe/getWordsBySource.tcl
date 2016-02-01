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

proc procCreateFilter {property operator value filter_list} {
	set isVar [isNotNull $value]
	if {$isVar} {
		set filter [java::new HashMap]
		$filter put "property" $property
		$filter put "operator" $operator
		$filter put "value" $value
		$filter_list add $filter
	}
}

proc procAddStringCriteria {search property filter_list} {
	set criteria [$search get $property]
	set criteriaNotNull [isNotNull $criteria]
	if {$criteriaNotNull} {
		java::try {
			set map [java::cast Map $criteria]
			set startsWith [$map get "startsWith"]
			set startsWithNotNull [isNotNull $startsWith]
			if {$startsWithNotNull} {
				procCreateFilter $property "startsWith" $startsWith $filter_list
			}

			set endsWith [$map get "endsWith"]
			set endsWithNotNull [isNotNull $endsWith]
			if {$endsWithNotNull} {
				procCreateFilter $property "endsWith" $endsWith $filter_list
			}

			set value [$map get "value"]
			set valueNotNull [isNotNull $value]
			if {$valueNotNull} {
				procCreateFilter $property "=" $value $filter_list
			}
		} catch {Exception err} {
    		puts "Error adding criteria for $property"
		}
	}
}

proc procAddNumberCriteria {search property filter_list} {
	set criteria [$search get $property]
	set criteriaNotNull [isNotNull $criteria]
	if {$criteriaNotNull} {
		java::try {
			set map [java::cast Map $criteria]
			set min [$map get "min"]
			set minNotNull [isNotNull $min]
			if {$minNotNull} {
				procCreateFilter $property ">=" $min $filter_list
			}

			set max [$map get "max"]
			set maxNotNull [isNotNull $max]
			if {$maxNotNull} {
				procCreateFilter $property "<=" $max $filter_list
			}

			set value [$map get "value"]
			set valueNotNull [isNotNull $value]
			if {$valueNotNull} {
				procCreateFilter $property "=" $value $filter_list
			}
		} catch {Exception err} {
    		puts "Error adding criteria for $property"
		}
		
	}
}

proc procAddListCriteria {search property filter_list} {
	set criteria [$search get $property]
	set criteriaNotNull [isNotNull $criteria]
	if {$criteriaNotNull} {
		java::try {
			set list [java::cast List $criteria]
			set size [$list size]
			if {$size > 0} {
				procCreateFilter $property "in" $list $filter_list
			}
		} catch {Exception err} {
    		puts "Error adding criteria for $property"
		}
	}
}

set object_type "Word"
set map [java::new HashMap]
$map put "nodeType" "DATA_NODE"
$map put "objectType" $object_type

set searchNotNull [isNotNull $filters]
if {$searchNotNull} {
	set filter_list [java::new ArrayList]
	procAddStringCriteria $filters "lemma" $filter_list
	procAddNumberCriteria $filters "syllableCount" $filter_list
	procAddNumberCriteria $filters "orthographic_complexity" $filter_list
	procAddNumberCriteria $filters "phonologic_complexity" $filter_list
	procAddListCriteria $filters "sources" $filter_list
	procAddListCriteria $filters "sourceTypes" $filter_list
	procAddListCriteria $filters "pos" $filter_list
	procAddListCriteria $filters "grade" $filter_list
	$map put "filters" $filter_list
}
set search_criteria [create_search_criteria $map]
set search_response [searchNodes $language_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	puts "Error response from searchNodes"
	return $search_response;
} else {
	set result_map [java::new HashMap]
	java::try {
		set graph_nodes [get_resp_value $search_response "node_list"]
		set word_list [java::new ArrayList]
		java::for {Node graph_node} $graph_nodes {
			set wordMetadataRes [lang_qe_getWordMetadata $graph_node]
			set wordMetadata [get_resp_value $wordMetadataRes "result"]
			$word_list add $wordMetadata
		}
		$result_map put "words" $word_list
	} catch {Exception err} {
    	puts [$err getMessage]
    	$result_map put "error" [$err getMessage]
	}
	set response_list [create_response $result_map]
	set response_csv [convert_response_to_csv $response_list "words"]
	return $response_csv
}




