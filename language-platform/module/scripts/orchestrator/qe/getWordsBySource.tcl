package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation


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

proc isNotEmpty {relations} {
	set exist false
	set hasRelations [java::isnull $relations]
	if {$hasRelations == 0} {
		set relationsSize [$relations size] 
		if {$relationsSize > 0} {
			set exist true
		}
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

proc procAddCountCriteria {obj prefix filter_list} {
	set mapNotNull [isNotNull $obj]
	if {$mapNotNull} {
		java::try {
			set map [java::cast Map $obj]
			set key_list [$map keySet]
			java::for {String key} $key_list {
				set val [$map get $key]
				set propValue [java::new String "count_"]
				set propValue [java::new String [$propValue concat $prefix]]
				set keyval [java::new String $key]
				set keyval [java::new String [$keyval trim]]
				set keyval [$keyval replaceAll "\\s+" "_"]
				set propValue [$propValue concat $keyval]
				puts "final propValue is $propValue"
				puts "size is $val"
				if {$val > 0} {
					procCreateFilter $propValue ">=" $val $filter_list
				}
			}
		} catch {Exception err} {
			puts "Error adding criteria for $property"
		}
	}
}


proc procCheckCountCriteria {search filter_list} {
	set criteria [$search get "count"]
	set criteriaNotNull [isNotNull $criteria]
	if {$criteriaNotNull} {
		java::try {
			set map [java::cast Map $criteria]
			set sourceTypes [$map get "sourceTypes"]
			set sources [$map get "sources"]
			set grades [$map get "grades"]
			set pos [$map get "pos"]

			procAddCountCriteria $sourceTypes "" $filter_list
			procAddCountCriteria $sources "source_" $filter_list
			procAddCountCriteria $grades "grade_" $filter_list
			procAddCountCriteria $pos "pos_" $filter_list
		} catch {Exception err} {
    		puts "Error adding criteria for $property"
		}
	}
}

proc procGetOutRelations {graph_node} {
	set outRelations [java::prop $graph_node "outRelations"]
	return $outRelations
}

proc procGetInRelations {graph_node} {
	set inRelations [java::prop $graph_node "inRelations"]
	return $inRelations
}

proc procGetSynonyms {inRelations} {
	set wordMap [java::new HashMap]
	set synsetIdList [java::new ArrayList]
	set synsetList [java::new ArrayList]
	java::for {Relation relation} $inRelations {
		if {[java::prop $relation "startNodeObjectType"] == "Synset" && [java::prop $relation "relationType"] == "synonym"} {
			set synsetId [java::prop $relation "startNodeId"]
			$synsetIdList add $synsetId

			set synsetMetadata [java::prop $relation "startNodeMetadata"]
			set checkMetadata [isNotNull $synsetMetadata]
			if {$checkMetadata} {
				set syn_gloss [$synsetMetadata get "gloss"]
				set checkGloss [isNotNull $syn_gloss]
				if {$checkGloss} {
					$synsetList add $syn_gloss	
				}
			}
		}
	}
	$wordMap put "Synonyms" $synsetList
	$wordMap put "Synset Ids" $synsetIdList
	return $wordMap
}

proc procGetAntonyms {outRelations} {
	set antonyms [java::new ArrayList]
	java::for {Relation relation} $outRelations {
		if {[java::prop $relation "endNodeObjectType"] == "Word" && [java::prop $relation "relationType"] == "hasAntonym"} {
			set nodeMetadata [java::prop $relation "endNodeMetadata"]
			set checkMetadata [isNotNull $nodeMetadata]
			if {$checkMetadata} {
				set antonym_lemma [$nodeMetadata get "lemma"]
				set checkLemma [isNotNull $antonym_lemma]
				if {$checkLemma} {
					$antonyms add $antonym_lemma	
				}
			}
		}
	}
	return $antonyms
}

proc procGetWordMetadata {node} {
	set wordMap [java::new HashMap]
	set isNodeNotNull [isNotNull $node]
	if {$isNodeNotNull} {
		set metadata [java::prop $node "metadata"]
		set checkMetadata [isNotNull $metadata]
		if {$checkMetadata} {
			$wordMap put "Lemma" [$metadata get "lemma"]
			$wordMap put "Syllable Count" [$metadata get "syllableCount"]
			$wordMap put "Phonological Complexity" [$metadata get "phonologic_complexity"]
			$wordMap put "Orthographic Complexity" [$metadata get "orthographic_complexity"]
			$wordMap put "Sources" [$metadata get "sources"]
			$wordMap put "Grades" [$metadata get "grade"]
			$wordMap put "POS Categories" [$metadata get "pos_categories"]
			$wordMap put "POS Tags" [$metadata get "pos"]
			$wordMap put "Source Types" [$metadata get "sourceTypes"]
			$wordMap put "Frequency" [$metadata get "occurrenceCount"]
			$wordMap put "Pronunciations" [$metadata get "pronunciations"]
			$wordMap put "Pictures" [$metadata get "pictures"]
			$wordMap put "Sample Usages" [$metadata get "sampleUsages"]
			$wordMap put "Plurality" [$metadata get "plurality"]
			$wordMap put "Cases" [$metadata get "cases"]
			$wordMap put "Genders" [$metadata get "genders"]
			$wordMap put "Variants" [$metadata get "variants"]
			$wordMap put "Inflections" [$metadata get "inflections"]
			$wordMap put "Grade 1 Count" [$metadata get "count_grade_1"]
			$wordMap put "Grade 2 Count" [$metadata get "count_grade_2"]
			$wordMap put "Grade 3 Count" [$metadata get "count_grade_3"]
			$wordMap put "Default Meaning" ""
		}
		$wordMap put "identifier" [java::prop $node "identifier"]
		set inRelations [procGetInRelations $node]
		set hasInRelations [isNotEmpty $inRelations]
		if {$hasInRelations} {
			set synsetMap [procGetSynonyms $inRelations]
			$wordMap putAll $synsetMap	
		}
		set outRelations [procGetOutRelations $node]
		set hasOutRelations [isNotEmpty $outRelations]
		if {$hasOutRelations} {
			set antonyms [procGetAntonyms $outRelations]
			$wordMap put "antonyms" $antonyms
		} else {
			$wordMap put "antonyms" [java::new ArrayList]
		}
	}
	return $wordMap
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
	procCheckCountCriteria $filters $filter_list
	$map put "filters" $filter_list
}
set limitNotNull [isNotNull $limit]
if {$limitNotNull} {
	$map put "resultSize" $limit	
}
set sortNotNull [isNotNull $sort]
if {$sortNotNull} {
	$map put "sortBy" $sort	
}
set orderNotNull [isNotNull $order]
if {$orderNotNull} {
	$map put "order" $order	
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
			set wordMetadata [procGetWordMetadata $graph_node]
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




