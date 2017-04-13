package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation
java::import  -package java.util.regex Pattern Matcher

proc isNotEmpty {list} {
	set exist false
	set isEmpty [java::isnull $list]
	if {$isEmpty == 0} {
		set listSize [$list size] 
		if {$listSize > 0} {
			set exist true
		}
	}
	return $exist
}


proc getWords { language_id startPosition resultSize } {

	set object_type "Word"
	set map [java::new HashMap]
	$map put "objectType" $object_type
	$map put "nodeType" "DATA_NODE"
	$map put "startPosition" [java::new Integer $startPosition]
	$map put "resultSize" [java::new Integer $resultSize]

	set search_criteria [create_search_criteria $map]
	set words [java::new ArrayList]

	set search_response [searchNodes $language_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
	} else {
		set graph_nodes [get_resp_value $search_response "node_list"]
		set words [java::cast List $graph_nodes]
	}

	return $words

}

proc getWord { language_id object_id} {

	set nodeRespone [getDataNode $language_id $object_id]
	set check_error [check_response_error $nodeRespone]
	if {$check_error} {
		return [java::new ArrayList]
	} else {
		set wordNode [get_resp_value $nodeRespone "node"]
		set words [java::new ArrayList]
		$words add $wordNode
	}
	return $words

}

proc retireSpecialCharacterWord {language_id word specialCharPattern} {
		# get inRelations of given word
		set wordIdentifier [java::prop $word "identifier"]
		set wordMetadata [java::prop $word "metadata"]
		set lemma [$wordMetadata get "lemma"]
		set lemma [$lemma toString]
		set hasSpecial [$specialCharPattern matcher [java::new String $lemma]]
		set matched [$hasSpecial matches]

		if {$matched} {
			$wordMetadata put "status" "Retired"
			set update_response [updateDataNode $language_id $wordIdentifier $word]
			set check_error [check_response_error $update_response]
			if {$check_error} {
				return $update_response;
			}
			puts "word $wordIdentifier got Retired"
		}

		return [java::null]
}

set startPosition 0
set resultSize 1000
set continue true

set specialChar {.*\[`~!@#$%^&*()_=+\\\[\\\]\{\}|\\\\;:'\",<.>/?-\].*}
set specialCharEsc [subst -nocommands -novariables $specialChar ]

set specialCharPattern [java::call Pattern compile $specialCharEsc]
puts "specialCharPattern [$specialCharPattern toString]"

while {$continue} {
	
	set words [getWords $language_id $startPosition $resultSize]
	puts "Words size : [$words size], startPosition $startPosition ,resultSize $resultSize"
	set hasWords [isNotEmpty $words]
	if {$hasWords} {
		java::for {Node word} $words {
			set id [java::prop $word "identifier"]
			set retireSpecialCharacterWordsResponse [retireSpecialCharacterWord $language_id $word $specialCharPattern]
			set retireSpecialCharacterWordsResponseNull [java::isnull $retireSpecialCharacterWordsResponse]
			if {$retireSpecialCharacterWordsResponseNull == 0} {
				set errorMsgMap [java::prop $retireSpecialCharacterWordsResponse "result"]
				puts "removeSpecialCharactersResponse exception while correcting Word -$id , error [$errorMsgMap toString]"
				return $retireSpecialCharacterWordsResponse
			} 
		}

	} else {
		set continue false
	}
	set startPosition [expr $startPosition + $resultSize]
}