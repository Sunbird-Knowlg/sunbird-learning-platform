package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package com.ilimi.graph.dac.model Node Relation

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

proc getRhymingsoundWordChains {rhymingSoundsetId graphId validWordIds wordScore minChainLength maxChainLength} {
		set totalScore 0
		set averageScore 0.0
		set chainLength 0
    set wordChains [java::new ArrayList]
		set title ""
		set relation "Rhyming rule"
		set setResponse [getSetMembers $graphId [$rhymingSoundsetId toString]]
		set check_error [check_response_error $setResponse]
		if {$check_error} {
			return $setResponse;
		} else {
			set rhymingSoundMembers [get_resp_value $setResponse "members"]
			set wordChain [java::new ArrayList]
			java::for {String memberID} $rhymingSoundMembers {
				set validMember [$validWordIds contains $memberID]
				if {$validMember == 1} {
						$wordChain add $memberID
						set score [$wordScore get $memberID]
						set scoreString [$score toString]
						set totalScore [expr $totalScore + $scoreString]
						set chainLength [expr $chainLength + 1]
						set removed [$validWordIds remove $memberID]
				}
        if {$chainLength >= $maxChainLength} {
            set averageScore [expr $totalScore/$chainLength]
						set wordChainRecord [java::new HashMap]
            $wordChainRecord put "title" $title
            $wordChainRecord put "list" $wordChain
            $wordChainRecord put "score" $averageScore
            $wordChainRecord put "relation" $relation
            $wordChains add $wordChainRecord
						set wordChain [java::new ArrayList]
            set chainLength 0
            set totalScore 0
        }
			}
			if {$chainLength < $minChainLength} {
				return $wordChains
			}
			set averageScore [expr $totalScore/$chainLength]
			set wordChainRecord [java::new HashMap]
			$wordChainRecord put "title" $title
			$wordChainRecord put "list" $wordChain
			$wordChainRecord put "score" $averageScore
			$wordChainRecord put "relation" $relation
			$wordChains add $wordChainRecord
			return $wordChains
		}
}

proc getRhymingSoundSet {word} {
		set inRelations [java::prop $word "inRelations"]
		set hasRelations [isNotEmpty $inRelations]
		if {$hasRelations} {
			java::for {Relation relation} $inRelations {
				set relationType [java::prop $relation "relationType"]
				set startNodeMetadata [java::prop $relation "startNodeMetadata"]
				set startNodeFuncObjectType [$startNodeMetadata get "IL_FUNC_OBJECT_TYPE"]
				set startNodeWordSetType [$startNodeMetadata get "type"]
				set isStartNodeWordSetTypeNull [java::isnull $startNodeWordSetType]
				if {($relationType == "hasMember") && ([$startNodeFuncObjectType toString] == "WordSet") &&
				   ($isStartNodeWordSetTypeNull == 0) && ([$startNodeWordSetType toString] == "RhymingSound")} {
					set rhymingSoundsetId [java::prop -noconvert $relation "startNodeId"]
					return [java::cast String $rhymingSoundsetId]
				}
			}
		}
		return [java::null]
}

set maxChainLengthObj [$ruleObject get "maxChainLength"]
set maxChainLength [$maxChainLengthObj toString]

set minChainLengthObj [$ruleObject get "minChainLength"]
set minChainLength [$minChainLengthObj toString]

set startWordsSize [$ruleObject get "startWordsSize"]
set ruleType [$ruleObject get "type"]

set wordsSize [$searchResult size]
set rhymingSound ""
set startWordsSizeString [$startWordsSize toString]

if {$wordsSize > $startWordsSizeString} {
	set topWordCount $startWordsSize
} else {
	set topWordCount $wordsSize
}

set count 0
set validWordIds [java::new ArrayList]
set wordScore [java::new HashMap]
set wordIdMap [java::new HashMap]


java::for {Map word} $searchResult {
	set id [$word get "identifier"]
	set score [$word get "score"]
	$wordScore put $id $score
	set nodeRespone [getDataNode $graphId $id]
	set check_error [check_response_error $nodeRespone]
	if {$check_error} {
		#set removed [$validWordIds remove $id]
	} else {
		set wordNode [get_resp_value $nodeRespone "node"]
		$validWordIds add $id
		$wordIdMap put $id $wordNode
	}
}

set listSize [$validWordIds size]
set wordChainCollection [java::new ArrayList]

while { ($topWordCount > $count) && ($listSize > $count) } {
		set id [$validWordIds get $count]
		set word [$wordIdMap get $id]
		set wordNode [java::cast Node $word]
		set rhymingSoundsetId [getRhymingSoundSet $wordNode]
		set isRhymingSoundSetIdNull [java::isnull $rhymingSoundsetId]
		if {$isRhymingSoundSetIdNull == 0} {
			set wordChains [getRhymingsoundWordChains $rhymingSoundsetId $graphId $validWordIds $wordScore $minChainLength $maxChainLength]
			if {$wordChains != ""} {
				set isWordChainNull [java::isnull $wordChains]
				if {$isWordChainNull == 0} {
					$wordChainCollection addAll $wordChains
				}
			}
		}
		set count [expr $count + 1 ]
		set listSize [$validWordIds size]
}

set sortedWordChains [sort_maps $wordChainCollection "score" "DESC"]
set finalWordChains [java::new ArrayList]
set wordChainsSize [$sortedWordChains size]
set wordChainsLimitString [$wordChainsLimit toString]
if { $wordChainsSize > $wordChainsLimitString} {
	set finalWordChains [$sortedWordChains subList 0 $wordChainsLimit]
} else {
	set finalWordChains $sortedWordChains
}

return $finalWordChains
