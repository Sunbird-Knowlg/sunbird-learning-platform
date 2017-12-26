package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node Relation


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

proc procGetOutRelations {graph_node} {
	set outRelations [java::prop $graph_node "outRelations"]
	return $outRelations
}

proc procGetInRelations {graph_node} {
	set inRelations [java::prop $graph_node "inRelations"]
	return $inRelations
}

proc procGetInRelationsByName {inRelations relationName} {
	set wordMap [java::new HashMap]
	java::for {Relation relation} $inRelations {
		if {[java::prop $relation "startNodeObjectType"] == "Synset" && [java::prop $relation "relationType"] == $relationName} {
			set synsetId [java::prop $relation "startNodeId"]
			set synsetMap [java::new HashMap]
			set synsetMetadata [java::prop $relation "startNodeMetadata"]
			set checkMetadata [isNotNull $synsetMetadata]
			if {$checkMetadata} {
				set syn_gloss [$synsetMetadata get "gloss"]
				set checkGloss [isNotNull $syn_gloss]
				if {$checkGloss} {
					$synsetMap put "gloss" $syn_gloss
				}

				set syn_pos [$synsetMetadata get "pos"]
				set checkPos [isNotNull $syn_pos]
				if {$checkPos} {
					$synsetMap put "pos" $syn_pos
				}
			}
			$wordMap put $synsetId $synsetMap
		}
	}
	return $wordMap
}

proc procGetOutRelationsByName {outRelations relationName} {
	set wordMap [java::new HashMap]
	java::for {Relation relation} $outRelations {
		if {[java::prop $relation "endNodeObjectType"] == "Synset" && [java::prop $relation "relationType"] == $relationName} {
			set synsetId [java::prop $relation "endNodeId"]
			set synsetMap [java::new HashMap]
			set synsetMetadata [java::prop $relation "endNodeMetadata"]
			set checkMetadata [isNotNull $synsetMetadata]
			if {$checkMetadata} {
				set syn_gloss [$synsetMetadata get "gloss"]
				set checkGloss [isNotNull $syn_gloss]
				if {$checkGloss} {
					$synsetMap put "gloss" $syn_gloss
				}

				set syn_pos [$synsetMetadata get "pos"]
				set checkPos [isNotNull $syn_pos]
				if {$checkPos} {
					$synsetMap put "pos" $syn_pos
				}
			}
			$wordMap put $synsetId $synsetMap
		}
	}
	return $wordMap
}

proc procGetMembers {outRelations} {
	set memberList [java::new ArrayList]
	java::for {Relation relation} $outRelations {
		if {[java::prop $relation "endNodeObjectType"] == "Word" && [java::prop $relation "relationType"] == "synonym"} {
			set wordId [java::prop $relation "endNodeId"]
			set wordMap [java::new HashMap]
			set metadata [java::prop $relation "endNodeMetadata"]
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
				$wordMap put "Grade 1 Count" [$metadata get "count_grade_1"]
				$wordMap put "Grade 2 Count" [$metadata get "count_grade_2"]
				$wordMap put "Grade 3 Count" [$metadata get "count_grade_3"]
			}
			$wordMap put "identifier" $wordId
			$memberList add $wordMap
		}
	}
	return $memberList
}

proc procGetHypernymMembers {language_id synsetId synsetObjMap hypernymId hypernymMap hypernym_list} {
	global prop_id
	global prop_gloss
	global prop_pos
	set hypernymObj [$hypernymMap get $hypernymId]
	set hypernymObjMap [java::cast Map $hypernymObj]						
	set get_hypernym_response [getDataNode $language_id $hypernymId]
	set hypernym_node [get_resp_value $get_hypernym_response "node"]
	set outRelations [procGetOutRelations $hypernym_node]
	set hasOutRelations [isNotEmpty $outRelations]
	if {$hasOutRelations} {
		set memberList [procGetMembers $outRelations]	

		java::for {Map memberMap} $memberList {
			set hypernym_word_map [java::new HashMap]
			$hypernym_word_map putAll $memberMap
			$hypernym_word_map put "Synset Id" $synsetId
			$hypernym_word_map put "Synset Gloss" [$synsetObjMap get "gloss"]
			$hypernym_word_map put "Synset Pos" [$synsetObjMap get "pos"]

			$hypernym_word_map put $prop_id $hypernymId
			$hypernym_word_map put $prop_gloss [$hypernymObjMap get "gloss"]
			$hypernym_word_map put $prop_pos [$hypernymObjMap get "pos"]

			$hypernym_list add $hypernym_word_map
		}
	}
	return $hypernym_list
}

global prop_id
global prop_gloss
global prop_pos

set relation_name "hasHypernym"
set inverse_relation_name "hasHyponym"
set prop_id "Hypernym Id"
set prop_gloss "Hypernym Gloss"
set prop_pos "Hypernym POS"
if {$relation == "hyponyms"} {
	set relation_name "hasHyponym"
	set inverse_relation_name "hasHypernym"
	set prop_id "Hyponym Id"
	set prop_gloss "Hyponym Gloss"
	set prop_pos "Hyponym POS"
} elseif {$relation == "meronyms"} {
	set relation_name "hasMeronym"
	set inverse_relation_name "hasHolonym"
	set prop_id "Meronym Id"
	set prop_gloss "Meronym Gloss"
	set prop_pos "Meronym POS"
} elseif {$relation == "holonyms"} {
	set relation_name "hasHolonym"
	set inverse_relation_name "hasMeronym"
	set prop_id "Holonym Id"
	set prop_gloss "Holonym Gloss"
	set prop_pos "Holonym POS"
}

set words [java::new ArrayList]
$words add $word
set resp_word_info [lang_getWordId $language_id $words]
set check_error [check_response_error $resp_word_info]
if {$check_error} {
	return $resp_word_info
} else {
	set word_ids [get_resp_value $resp_word_info "word_ids"]
	set word_ids_not_empty [isNotEmpty $word_ids]
	if {$word_ids_not_empty} {
		set word_id_map [$word_ids get $word]
		set map [java::cast Map $word_id_map]
		set word_id [$map get "wordId"]

		set get_node_response [getDataNode $language_id $word_id]
		set graph_node [get_resp_value $get_node_response "node"]
		set inRelations [procGetInRelations $graph_node]
		set hasInRelations [isNotEmpty $inRelations]
		set member_id_list [java::new ArrayList]
		set hypernym_list [java::new ArrayList]
		if {$hasInRelations} {
			set synsetMap [procGetInRelationsByName $inRelations "synonym"]
			set synsetIds [$synsetMap keySet]
			java::for {String synsetId} $synsetIds {
				set synsetObj [$synsetMap get $synsetId]
				set synsetObjMap [java::cast Map $synsetObj]
				set get_synset_response [getDataNode $language_id $synsetId]
				set synset_node [get_resp_value $get_synset_response "node"]
				set outRelations [procGetOutRelations $synset_node]
				set hasOutRelations [isNotEmpty $outRelations]
				if {$hasOutRelations} {
					set hypernymMap [procGetOutRelationsByName $outRelations $relation_name]
					set hypernymIds [$hypernymMap keySet]
					java::for {String hypernymId} $hypernymIds {
						$member_id_list add $hypernymId
						set hypernym_list [procGetHypernymMembers $language_id $synsetId $synsetObjMap $hypernymId $hypernymMap $hypernym_list]
					}
				}
				set inRelations [procGetInRelations $synset_node]
				set hasInRelations [isNotEmpty $inRelations]
				if {$hasInRelations} {
					set hyponymMap [procGetInRelationsByName $inRelations $inverse_relation_name]
					set hyponymIds [$hyponymMap keySet]
					java::for {String hyponymId} $hyponymIds {
						set check_member [$member_id_list contains $hyponymId]
						if {!$check_member} {
							$member_id_list add $hyponymId
							set hypernym_list [procGetHypernymMembers $language_id $synsetId $synsetObjMap $hyponymId $hyponymMap $hypernym_list]
						}
					}
				}
			}
		}

		set result_map [java::new HashMap]
		$result_map put "hypernyms" $hypernym_list
		set response_list [create_response $result_map]
		set response_csv [convert_response_to_csv $response_list "hypernyms"]
		return $response_csv
	} else {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_WORD_NOT_FOUND"
		$result_map put "responseCode" [java::new Integer 404]
		set response_list [create_error_response $result_map]
		return $response_list
	}
}


