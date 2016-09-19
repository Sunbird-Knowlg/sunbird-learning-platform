
package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package java.util Collection
java::import -package com.ilimi.graph.dac.model Node Relation
# java::import -package org.apache.commons.lang3.StringUtils

# function to get character/unicode at any given index position
proc characterAt {word index graphId} {

		if {[$graphId toString] == "en"} {
			set metadata [java::prop $word "metadata"]
			set wordText [$metadata get "lemma"]
			set wordText [java::cast String $wordText]
			set wordlength [$wordText length]
			if {$index == 999} {
				set index [expr $wordlength - 1 ]
			} 
			if {$index >= $wordlength} {
				return ""
			}
			set character [java::call String valueOf [$wordText charAt $index]]
			return $character

		} else {
			set metadata [java::prop $word "metadata"]
			set unicodeNotation [$metadata get "unicodeNotation"]
			set unicodeNotation [java::cast String $unicodeNotation]
			set syllables [$unicodeNotation split "\\s+"]
			set arr_instance [java::instanceof $syllables {String[]}]
			if {$arr_instance == 1} {			
				set syllables [java::cast {String[]} $syllables]
				set syllablesLength [$syllables length]
				if {$index == 999} {
					set index [expr $syllablesLength - 1 ]
				}
				if {$index >= $syllablesLength} {
					return ""
				}
				set syllable [$syllables get $index]
				set syllable [java::new String $syllable]
				set syllableUnicodes [$syllable split "\\\\" ]
				set arr_instance [java::instanceof $syllableUnicodes {String[]}]
				set firstUnicode ""
				if {$arr_instance == 1} {
					set syllableUnicodes [java::cast {String[]} $syllableUnicodes]
					if { [$syllableUnicodes length] > 1} {
						set firstUnicode [$syllableUnicodes get 1]	
					}
				}
				return $firstUnicode
			} else {
				return ""
			}
		}

}

# get index from rule object and set it as index_posistion
set indexObj [$ruleObject get "index"]
set indexStr [java::cast String $indexObj]
set indexStr [$indexStr toUpperCase]
set index 0

set isLast [string equal $indexStr "LAST"]
if {$isLast == 1} {
	set index 999
} else {
	set index [expr $index + $indexStr ]
}
# get maxChainLength from rule object and set it as maxChainLength
set maxChainLengthObj [$ruleObject get "maxChainLength"]
set maxChainLength [$maxChainLengthObj toString]

# get minChainLength from rule object and set it as minChainLength
set minChainLengthObj [$ruleObject get "minChainLength"]
set minChainLength [$minChainLengthObj toString]

# Key-Character/unicode of given index at word , value - list of words
set wordChainMap [java::new HashMap]
set wordsSize [$searchResult size]
set ruleName [$ruleObject get "identifier"]

# loop through searchResult and prepare WordMap based on its character at given index position
java::for {Map word} $searchResult {
	set id [$word get "identifier"]
	# add score against id in wordScore map
	set score [$word get "score"]
	set isScoreNull [java::isnull $score]
	if {$isScoreNull == 1} {
		set score 1.0
	}

	# get WordNode based on its id
	set nodeRespone [getDataNode $graphId $id]
	set check_error [check_response_error $nodeRespone]
	if {$check_error} {
		#remove wordId from validWordIds if word is not found
		#skip
	} else {
		set wordResponse [get_resp_value $nodeRespone "node"]
		set wordNode [java::cast Node $wordResponse]
		set character [characterAt $wordNode $index $graphId]
		# check character is not null
		#set isCharacterNull [java::isnull $character]
		# 0 is false and 1 is true in the above statement
		set emptyString ""
		set isEmpty [string equal $character $emptyString]
		if {$isEmpty == 0} {
			set wordChain [$wordChainMap get $character] 
			if {[java::isnull $wordChain]} {
				set wordChain [java::new HashMap]
				set wordList [java::new ArrayList]
				set totalScore 0
			} else {
				set wordChain [java::cast Map $wordChain]
				set wordList [$wordChain get "list"]
				set wordList [java::cast List $wordList]
				set totalScore [$wordChain get "score"]
				set totalScore [$totalScore toString]
			}
			set wordListLength [$wordList size] 
			if {$wordListLength < $maxChainLength} {
				$wordList add $id
				set totalScore [expr $totalScore + $score]
				$wordChain put "list" $wordList
				$wordChain put "score" $totalScore
				$wordChain put "relation" $ruleName
				$wordChainMap put $character $wordChain
			}	
		
		}
	}
}

set wordChainCollection [$wordChainMap values]
set wordChainCollection [java::cast Collection $wordChainCollection]
set wordChainList [java::new ArrayList $wordChainCollection] 
set wordChains [java::new ArrayList]

# filter wordChain that meets minimum word chain length
java::for {Map wordChain} $wordChainList {
		set wordList [$wordChain get "list"]
		set wordList [java::cast List $wordList]
		set wordChainLength [$wordList size]
		if {$wordChainLength >= $minChainLength} {
			$wordChains add $wordChain
		}
}

# sort the wordChains based on score(average)
set sortedWordChains [sort_maps $wordChains "score" "DESC"]
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

