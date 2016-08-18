package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package com.ilimi.graph.dac.model Node Relation

#function to check whether list is empty or has some data
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

#function to get wordChains based on RhymingSound set and min/max chainLength
proc getRhymingsoundWordChains {rhymingSoundSetId graphId validWordIds wordScore minChainLength maxChainLength ruleName} {
		set totalScore 0
		set averageScore 0.0
		set chainLength 0
    set wordChains [java::new ArrayList]
		set title ""
		# getSetMembers of given RhymingSound set
		set setResponse [getSetMembers $graphId [$rhymingSoundSetId toString]]
		set check_error [check_response_error $setResponse]
		if {$check_error} {
			return [java::null]
		} else {
			set rhymingSoundMembers [get_resp_value $setResponse "members"]
			set wordChain [java::new ArrayList]
			# loop through members of RhymingSound set
			java::for {String memberID} $rhymingSoundMembers {
				set validMember [$validWordIds contains $memberID]
				if {$validMember == 1} {
						# add member into wordChain list
						$wordChain add $memberID
						# compute totalScore by adding the new member score
						set score [$wordScore get $memberID]
						set scoreString [$score toString]
						set totalScore [expr $totalScore + $scoreString]
						# add chainLength
						set chainLength [expr $chainLength + 1]
						# remove the member from ValidWordIDS as to avoid processing them again in main function
						set removed [$validWordIds remove $memberID]
				}
				# add wordChain into list if its length crossed max_chain_length
        if {$chainLength >= $maxChainLength} {
            set averageScore [expr $totalScore/$chainLength]
						set wordChainRecord [java::new HashMap]
            $wordChainRecord put "title" $title
            $wordChainRecord put "list" $wordChain
            $wordChainRecord put "score" $averageScore
            $wordChainRecord put "relation" $ruleName
            $wordChains add $wordChainRecord
						# reset wordChain, length and score variables
						set wordChain [java::new ArrayList]
            set chainLength 0
            set totalScore 0
        }
			}
		  # current chain length is less than min_chain_length ignore them, return wordChains those were added in the last loop
			if {$chainLength < $minChainLength} {
				return $wordChains
			}
		  # add wordchain into existing list(wordChains)
			set averageScore [expr $totalScore/$chainLength]
			set wordChainRecord [java::new HashMap]
			$wordChainRecord put "title" $title
			$wordChainRecord put "list" $wordChain
			$wordChainRecord put "score" $averageScore
			$wordChainRecord put "relation" $ruleName
			$wordChains add $wordChainRecord
			return $wordChains
		}
}

# function to get RhymingSoundSet of given word
proc getRhymingSoundSet {word} {
		# get inRelations of given word
		set inRelations [java::prop $word "inRelations"]
		set hasRelations [isNotEmpty $inRelations]
		if {$hasRelations} {
			java::for {Relation relation} $inRelations {
				# if relation is of type 'hasMember' and startNode is of ObjectType 'WordSet'  and of type 'RhymingSound'
				# return that start_node_id as rhymingSoundId
				set relationType [java::prop $relation "relationType"]
				set startNodeMetadata [java::prop $relation "startNodeMetadata"]
				set startNodeFuncObjectType [$startNodeMetadata get "IL_FUNC_OBJECT_TYPE"]
				set startNodeWordSetType [$startNodeMetadata get "type"]
				set isStartNodeWordSetTypeNull [java::isnull $startNodeWordSetType]
				if {($relationType == "hasMember") && ([$startNodeFuncObjectType toString] == "WordSet") &&
				   ($isStartNodeWordSetTypeNull == 0) && ([$startNodeWordSetType toString] == "RhymingSound")} {
					set rhymingSoundSetId [java::prop -noconvert $relation "startNodeId"]
					return [java::cast String $rhymingSoundSetId]
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
set ruleName [$ruleObject get "name"]

set wordsSize [$searchResult size]
set rhymingSound ""
set startWordsSizeString [$startWordsSize toString]

if {$wordsSize > $startWordsSizeString} {
	set topWordCount $startWordsSizeString
} else {
	set topWordCount $wordsSize
}

set count 0
set validWordIds [java::new ArrayList]
set wordScore [java::new HashMap]
set wordIdMap [java::new HashMap]

# loop through searchResult and prepare ValidWordIDS list, WordScore Map and WordIDMap
java::for {Map word} $searchResult {
	set id [$word get "identifier"]
	# add score against id in wordScore map
	set score [$word get "score"]
	set isScoreNull [java::isnull $score]
	if {$isScoreNull == 1} {
		set score 1.0
	}
	$wordScore put $id $score
	# get WordNode based on its id
	set nodeRespone [getDataNode $graphId $id]
	set check_error [check_response_error $nodeRespone]
	if {$check_error} {
		#skip
	} else {
		set wordNode [get_resp_value $nodeRespone "node"]
		# add Id into validWordIds list and add wordNode against id in wordIDMap
		$validWordIds add $id
		$wordIdMap put $id $wordNode
	}
}

set listSize [$validWordIds size]
set wordChainCollection [java::new ArrayList]

# loop until count not reached to topWordCount and listSize is not reduced to count
while { ($topWordCount > $count) && ($listSize > $count) } {
		set id [$validWordIds get $count]
		set wordObj [$wordIdMap get $id]
		set wordNode [java::cast Node $wordObj]
		set rhymingSoundSetId [getRhymingSoundSet $wordNode]
		set isRhymingSoundSetIdNull [java::isnull $rhymingSoundSetId]
		if {$isRhymingSoundSetIdNull == 0} {
			# get wordChains of rhymingSound
			set wordChains [getRhymingsoundWordChains $rhymingSoundSetId $graphId $validWordIds $wordScore $minChainLength $maxChainLength $ruleName]
			set hasWordChain [isNotEmpty $wordChains]
			if { $hasWordChain } {
				# add all wordChains into wordChainCollection
				$wordChainCollection addAll $wordChains
			}
		}
		set count [expr $count + 1 ]
		set listSize [$validWordIds size]
}

# sort the wordChains based on score(average)
set sortedWordChains [sort_maps $wordChainCollection "score" "DESC"]
set finalWordChains [java::new ArrayList]
set wordChainsSize [$sortedWordChains size]
set wordChainsLimitString [$wordChainsLimit toString]
if { $wordChainsSize > $wordChainsLimitString} {
	# sublist sortedWordChains based on given wordChainsLimit
	set finalWordChains [$sortedWordChains subList 0 $wordChainsLimit]
} else {
	set finalWordChains $sortedWordChains
}

return $finalWordChains
