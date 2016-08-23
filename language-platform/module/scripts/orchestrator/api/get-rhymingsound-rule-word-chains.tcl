#This script does the following
# 1. Create word chain list based on minimum and maximum word chain length with average score
# 2. Sorts results based on score in desc order
# 3. returns results
#
#Input:
#1. Graph Id (language)
#2. Rule Object (Map<String, Object>)
#3. Search Result (List<Map<String, Object>>) (List of words)
#4. Word Chains Limit
#
#Output
# 1. Word Chains - List<Map<String, Object>>
# ex: [
#       {
#			"list": ["en_1", "en_2"],
#			"score": 2.0,
#			"relation": "RhymingSound"
#		    }
#    ]
package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package com.ilimi.graph.dac.model Node Relation

#function to check whether list is empty or not
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
 		# chainLength holds current word chain length
		set chainLength 0
		# wordChains is list
    set wordChains [java::new ArrayList]
		# getSetMembers of given RhymingSound set
		set setResponse [getSetMembers $graphId [$rhymingSoundSetId toString]]
		# check whether getSetMembers response is success or error
		set check_error [check_response_error $setResponse]
		if {$check_error} {
			# return null as the response is error
			return [java::null]
		} else {
			# get members from getSetMembers response
			set rhymingSoundMembers [get_resp_value $setResponse "members"]
			set wordChain [java::new ArrayList]
			# loop through for each member of RhymingSound set
			java::for {String memberID} $rhymingSoundMembers {
				# check whether the member of rhymingSound set is exist in search result(validWordIds)
				set validMember [$validWordIds contains $memberID]
				if {$validMember == 1} {
						# add rhymingSound member into wordChain list
						$wordChain add $memberID
						# compute totalScore by adding the new member score
						set score [$wordScore get $memberID]
						set scoreString [$score toString]
						set totalScore [expr $totalScore + $scoreString]
						# increment chainLength by 1
						set chainLength [expr $chainLength + 1]
						# remove the member from ValidWordIDS as to avoid processing them again in main function
						set removed [$validWordIds remove $memberID]
				}
				# freeze wordChain and add it into list(wordChains) if its length crossed max_chain_length
        if {$chainLength >= $maxChainLength} {
						# compute average score
						set averageScore [expr $totalScore/$chainLength]
						set wordChainRecord [java::new HashMap]
						$wordChainRecord put "list" $wordChain
						$wordChainRecord put "score" $averageScore
						$wordChainRecord put "relation" $ruleName
						# add current wordChain into wordChains list
						$wordChains add $wordChainRecord
						# reset wordChain, length and score variables
						set wordChain [java::new ArrayList]
						set chainLength 0
						set totalScore 0
        }
				# end of the loop
			}
		  # current chain length is less than min_chain_length ignore them, return existing wordChains
			if {$chainLength < $minChainLength} {
				return $wordChains
			}
		  # add wordchain into existing wordChains and return wordChains
			set averageScore [expr $totalScore/$chainLength]
			set wordChainRecord [java::new HashMap]
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

# get maxChainLength from rule object and set it as maxChainLength
set maxChainLengthObj [$ruleObject get "maxChainLength"]
set maxChainLength [$maxChainLengthObj toString]

# get minChainLength from rule object and set it as minChainLength
set minChainLengthObj [$ruleObject get "minChainLength"]
set minChainLength [$minChainLengthObj toString]

# get startWordsSize from rule object and set it as startWordsSize(No of words to start from search result)
set startWordsSize [$ruleObject get "startWordsSize"]
set startWordsSizeString [$startWordsSize toString]

# get searchResult size and set it as wordsSize
set wordsSize [$searchResult size]
# set topWordCount as minimum of startWordSize or wordsSize
if {$wordsSize > $startWordsSizeString} {
	set topWordCount $startWordsSizeString
} else {
	set topWordCount $wordsSize
}

set validWordIds [java::new ArrayList]
set wordScore [java::new HashMap]
set wordIdMap [java::new HashMap]

# loop through searchResult and prepare ValidWordIDS list, WordScore Map and WordIDMap
# ValidWordIDS has all search result words IDs
# WordScore map contains key value pair as wordId and Score
# WordIDMap map contains key value pair as wordId and Word Object
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

# set listSize as search result words count
set listSize [$validWordIds size]
set wordChainCollection [java::new ArrayList]
# set ruleName from Rule Object indetifier for setting it word chain result
set ruleName [$ruleObject get "identifier"]
# count holds values of current iteration
set count 0

# loop until count not reached to topWordCount and listSize is not reduced to count
while { ($topWordCount > $count) && ($listSize > $count) } {
		# get id from master search result(ValidWordIds)
		set id [$validWordIds get $count]
		# get word Object from wordIdMap and cast it to Node type
		set wordObj [$wordIdMap get $id]
		set wordNode [java::cast Node $wordObj]
		# call getRhymingSoundSet procedure to get rhymingSound setId
		set rhymingSoundSetId [getRhymingSoundSet $wordNode]
		# check rhymingSoundSetId is not null
		set isRhymingSoundSetIdNull [java::isnull $rhymingSoundSetId]
		# 0 is false and 1 is true in the above statement
		if {$isRhymingSoundSetIdNull == 0} {
			# get wordChains of rhymingSound
			set wordChains [getRhymingsoundWordChains $rhymingSoundSetId $graphId $validWordIds $wordScore $minChainLength $maxChainLength $ruleName]
			set hasWordChain [isNotEmpty $wordChains]
			if { $hasWordChain } {
				# add all wordChains into wordChainCollection
				$wordChainCollection addAll $wordChains
			}
		}
		#increment count by 1
		set count [expr $count + 1 ]
		#get current size of validWordIds and set it as listSize
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
