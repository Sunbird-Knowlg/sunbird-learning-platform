package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node
java::import -package org.ekstep.language.measures WordMeasures
java::import -package org.ekstep.language.measures.entity WordComplexity

proc proc_getWords {language_id startPosition resultSize} {
	set nodes [java::new ArrayList]
	set search [java::new HashMap]
	$search put "objectType" "Word"
	$search put "nodeType" "DATA_NODE"
	$search put "startPosition" [java::new Integer $startPosition]
	$search put "resultSize" [java::new Integer $resultSize]
	set search_criteria [create_search_criteria $search]
	set search_response [searchNodes $language_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		puts "error searching words"
	} else {
		set graph_nodes [get_resp_value $search_response "node_list"]
		java::for {Node graph_node} $graph_nodes {
			$nodes add $graph_node
		}
	}
	return $nodes
}

set minVal [java::call Integer parseInt $min 16]
set maxVal [java::call Integer parseInt $max 16]
set words [java::new ArrayList]
set count 1
set startPosition 0
set resultSize 5000
while {$count > 0} {
	set nodeList [proc_getWords $language_id $startPosition $resultSize]
	puts "batch completed - $startPosition"
	set size [$nodeList size]
	if {$size > 0} {
		java::for {Node graph_node} $nodeList {
			set identifier [java::prop $graph_node identifier]	
			set metadata [java::prop -noconvert $graph_node metadata]
			set lemma [$metadata get "lemma"]	
			set lemma [[java::new String [$lemma toString]] trim]
			set lemma [java::new String $lemma]
			set char [$lemma charAt 0]
			scan $char "%c" num
			set intCode [java::new Integer $num]
			set arr [java::new {Integer[]} {1}]
			$arr set 0 $intCode
			set uc [java::call -noconvert String format "%04x" $arr]
			java::try {
				set ucVal [java::call Integer parseInt $uc 16]
				if {$ucVal < $minVal || $ucVal > $maxVal} {
					puts "deleting - $lemma - $identifier"
					$words add $lemma
					set delete_response [deleteDataNode $language_id $identifier]
				}	
			} catch {Exception err} {
				puts "deleting - $lemma - $identifier"
		    	$words add $lemma
		    	set delete_response [deleteDataNode $language_id $identifier]
			}
		}	
	} else {
		set count 0
    	break
	}
	set startPosition [expr {$startPosition+5000}]
}
set wsize [$words size]
puts "words - $wsize"
return $words
