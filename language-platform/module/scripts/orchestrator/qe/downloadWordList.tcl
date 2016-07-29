package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation

proc proc_isEmpty {value} {
	set exist false
	java::try {
		set hasValue [java::isnull $value]
		if {$hasValue == 1} {
			set exist true
		} else {
			set strValue [$value toString]
			set newStrValue [java::new String $strValue]
			set strLength [$newStrValue length]
			if {$strLength == 0} {
				set exist true
			}
		}
	} catch {Exception err} {
    	set exist true
	}
	return $exist
}

proc proc_isListNotEmpty {relations} {
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

proc procGetPrimaryMeaningId {wordMetadata} {
	set primaryMeaningId [$wordMetadata get "primaryMeaningId"]
	set isEmpty [proc_isEmpty $primaryMeaningId]
	if {!$isEmpty} {
		return [java::new String [$primaryMeaningId toString]]
	} else {
		return [java::new String]
	}
}

proc procGetWordData {wordId wordMetadata} {
	set map [java::new HashMap]
	$map put "identifier" $wordId
	$map put "lemma" [$wordMetadata get "lemma"]
	$map put "status" [$wordMetadata get "status"]
	$map put "grade" [$wordMetadata get "grade"]
	$map put "pronunciations" [$wordMetadata get "pronunciations"]
	$map put "hasConnotative" [$wordMetadata get "hasConnotative"]
	$map put "isPhrase" [$wordMetadata get "isPhrase"]
	$map put "isLoanWord" [$wordMetadata get "isLoanWord"]
	$map put "orthographic_complexity" [$wordMetadata get "orthographic_complexity"]
	$map put "phonologic_complexity" [$wordMetadata get "phonologic_complexity"]
	$map put "exampleSentences" [$wordMetadata get "exampleSentences"]
	return $map
}

proc procGetSynsetNode {language_id wordMap synsetId} {
	set getDataNodeResp [getDataNode $language_id $synsetId]
	set get_node_response_error [check_response_error $getDataNodeResp]
	if {!$get_node_response_error} {
		set graph_node [get_resp_value $getDataNodeResp "node"]
		$wordMap put "tags" [java::prop $graph_node "tags"]
		set outRelations [java::prop $graph_node "outRelations"]
		set hasOutRelations [proc_isListNotEmpty $outRelations]
		if {$hasOutRelations} {
			set relMap [java::new HashMap]
			java::for {Relation relation} $outRelations {
				if {[java::prop $relation "endNodeObjectType"] == "Word"} {
					set relType [java::prop $relation "relationType"]
					set endMetadata [java::prop $relation "endNodeMetadata"]
					set lemmas [$relMap get $relType]
					set isListNull [java::isnull $lemmas]
					if {$isListNull == 1} {
						set lemmas [java::new ArrayList]
						$lemmas add [$endMetadata get "lemma"]
						$relMap put $relType $lemmas
					} else {
						set isListInstance [java::instanceof $lemmas List]
						if {$isListInstance == 1} {
							set lemmaList [java::cast List $lemmas]
							$lemmaList add [$endMetadata get "lemma"]
						}
					}
				}
			}
			$wordMap putAll $relMap
		}
	}
}

proc procAddSynsetData {language_id synsetId synsetMetadata primaryMeaningId wordMap word_list} {
	$wordMap put "synsetId" $synsetId
	$wordMap put "gloss" [$synsetMetadata get "gloss"]
	$wordMap put "pos" [$synsetMetadata get "pos"]
	$wordMap put "pictures" [$synsetMetadata get "pictures"]
	$wordMap put "category" [$synsetMetadata get "category"]
	$wordMap put "gender" [$synsetMetadata get "gender"]
	if {[$primaryMeaningId equals $synsetId]} {
		$wordMap put "primaryMeaning" true
	} else {
		$wordMap put "exampleSentences" [$synsetMetadata get "exampleSentences"]
	}
	procGetSynsetNode $language_id $wordMap $synsetId
	$word_list add $wordMap
}

proc procGetWordListMembers {language_id getDataNodesResp} {
	set nodes [get_resp_value $getDataNodesResp "node_list"]
	set word_list [java::new ArrayList]
	java::for {Node node} $nodes {
		set wordId [java::prop $node "identifier"]
		set wordMetadata [java::prop $node "metadata"]
		set primaryMeaningId [procGetPrimaryMeaningId $wordMetadata]
		set inRelations [java::prop $node "inRelations"]
		set hasInRelations [proc_isListNotEmpty $inRelations]
		if {$hasInRelations} {
			java::for {Relation relation} $inRelations {
				if {[java::prop $relation "startNodeObjectType"] == "Synset" && [java::prop $relation "relationType"] == "synonym"} {
					set wordMap [procGetWordData $wordId $wordMetadata]
					set synsetId [java::prop $relation "startNodeId"]
					set synsetMetadata [java::prop $relation "startNodeMetadata"]
					procAddSynsetData $language_id $synsetId $synsetMetadata $primaryMeaningId $wordMap $word_list
				}
			}
		}
	}
	return $word_list
}

set object_type "WordList"

set membersResp [getSetMembers $language_id $wordlist]
set check_error [check_response_error $membersResp]
set result_map [java::new HashMap]
if {$check_error} {
	return $membersResp;
} else {
	set member_ids [get_resp_value $membersResp "members"]
	set members_size [$member_ids size]
	set getDataNodesResp [getDataNodes $language_id $member_ids]
	set check_error [check_response_error $getDataNodesResp]
	if {$check_error} {
		return $getDataNodesResp;
	} else {
		set word_list [procGetWordListMembers $language_id $getDataNodesResp]
		$result_map put "words" $word_list
		set response_list [create_response $result_map]
		set response_csv [convert_response_to_csv $response_list "words"]
		return $response_csv
	}
}
