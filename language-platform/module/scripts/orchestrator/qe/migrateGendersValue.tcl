package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node Relation
java::import -package java.util Arrays

set object_type "Word"

proc isNotEmpty {list} {
	set exist false
	set isListNull [java::isnull $list]
	if {$isListNull == 0} {
		set listSize [$list size]
		if {$listSize > 0} {
			set exist true
		}
	}
	return $exist
}

proc getDefinitionFromCache {defMap language_id object_type} {

	set definition [$defMap get $language_id]
	set definitionNull [java::isnull $definition]
	if {$definitionNull == 1} {
		set resp_def_node [getDefinition $language_id $object_type]
		set def_node [get_resp_value $resp_def_node "definition_node"]
		$defMap put $language_id $def_node
		set definition $def_node
	}
	return $definition
}

proc migrateGendersValue {word_metadata} {
	set genders [$word_metadata get "genders"]	
	set isGendersNull [java::isnull $genders]
	if {$isGendersNull == 0} {
		set arr_instance [java::instanceof $genders {String[]}]
		if {$arr_instance == 1} {
			set genders [java::cast {String[]} $genders]
			set genders [java::call Arrays asList $genders]
		} else {
			set genders [java::cast ArrayList $genders]
		}

		if {[isNotEmpty $genders]} {
			set new_genders [java::new ArrayList]
			java::for {String gender} $genders {
			    if {$gender == "f"} {
			    	$new_genders add "female"
		    	} elseif {$gender == "m"} {
		    		$new_genders add "male"
		    	} elseif {$gender == "n"} {
		    		$new_genders add "neutral"
		    	} else {
		    		$new_genders add $gender
		    	}
			}
			#puts "new_genders [$new_genders toString] gender [$genders toString]"
			$word_metadata put "genders" $new_genders
			return true
		}
	}

	return false

}

proc getWords {} {
	set filters [java::new HashMap]
	$filters put "objectType" "Word"
	set status [java::new ArrayList]
	$filters put "status" $status
	set limit [java::new Integer 10000]
	set exists [java::new ArrayList]
	$exists add "genders"

	set searchCriteria [java::new HashMap]
	$searchCriteria put "filters" $filters
	$searchCriteria put "exists" $exists
	$searchCriteria put "limit" $limit

	#puts "searchCriteria  [$searchCriteria toString]"
	set searchResponse [compositeSearch $searchCriteria]
	set searchResultsMap [$searchResponse getResult]
	set wordsList [java::cast List [$searchResultsMap get "results"]]

	return $wordsList
}

proc correctWordHavingGender {} {

	set object_type "Word"
	set wordsList [getWords]
	set wordsListNull [java::isnull $wordsList]
	set defMap [java::new HashMap]

	if {$wordsListNull == 0 && [$wordsList size] >= 0} {
		#puts "wordsList size [$wordsList size]"
		java::for {Object wordObj} $wordsList {
			set wordObject [java::cast Map $wordObj]		
			set identifier [$wordObject get "identifier"]
			set language_id [$wordObject get "graph_id"]

				set nodeRespone [getDataNode $language_id $identifier]
				set check_error [check_response_error $nodeRespone]
				if {$check_error} {
					return $nodeRespone
				} else {
					set wordResponse [get_resp_value $nodeRespone "node"]
					set wordNode [java::cast Node $wordResponse]
					set metadata [java::prop $wordNode "metadata"]
					set isChanged [migrateGendersValue $metadata]
					if {$isChanged} {
						set word [java::new HashMap]
						$word put "identifier" $identifier
						$word put "objectType" $object_type
						$word put "genders" [$metadata get "genders"]
						set def_node [getDefinitionFromCache $defMap $language_id $object_type]
						set word_obj [convert_to_graph_node $word $def_node]

						set update_response [updateDataNode $language_id $identifier $word_obj]
						set check_error [check_response_error $update_response]
						if {$check_error} {
							return $update_response;
						}
						puts "word identifier $identifier language_id=$language_id migrated"
					}
				}

		}
		#puts "wordsList Done"
	}

	return [java::null]

}

set result [correctWordHavingGender]
set resultNull [java::isnull $result]

if {$resultNull == 0} {
	#return error response
	set resultMap [java::prop $result "result"]
	#puts " error message [$resultMap toString]"
	return $result
}


set result_map [java::new HashMap]
$result_map put "status" "OK"
set response_list [create_response $result_map]
return $response_list