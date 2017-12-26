package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util Date
java::import -package org.ekstep.graph.dac.model Node

proc proc_isNotNull {value} {
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

proc proc_getFirstElement {input_list} {
	puts "getting first element"
	set listNotNull [proc_isNotNull $input_list]
	if {$listNotNull} {
		set arr_instance [java::instanceof $input_list {String[]}]
		if {$arr_instance == 1} {
			set array [java::cast {String[]} $input_list]
			set listSize [$array length] 
			if {$listSize > 0} {
				set word [$array get 0]
				return $word
			} else {
				return [java::null]
			}
		} else {
			return [$input_list toString]
		}
	} else {
		return [java::null]
	}
}

proc proc_searchNodes {startPosition resultSize} {
	set object_type "Content"
	set graph_id "domain"
	set map [java::new HashMap]
	$map put "nodeType" "DATA_NODE"
	$map put "objectType" $object_type

	set statusList [java::new ArrayList]
	$statusList add "Live"
	$map put "status" $statusList

	set contentTypes [java::new ArrayList]
	$contentTypes add "Story"
	$contentTypes add "Worksheet"
	$contentTypes add "Collection"
	$contentTypes add "Game"
	$contentTypes add "TextBook"

	$map put "contentType" $contentTypes
	$map put "startPosition" [java::new Integer $startPosition]
	$map put "resultSize" [java::new Integer $resultSize]

	set search_criteria [create_search_criteria $map]
	set search_response [searchNodes $graph_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		set params [java::prop $search_response "params"]
		if {[java::isnull $params] == 0} {
			set msg [java::prop $params "errmsg"]
		}
		set graph_nodes [java::null]
		return $graph_nodes
	} else {
		set graph_nodes [get_resp_value $search_response "node_list"]
		return $graph_nodes
	}
}

set graph_id "domain"
set count 1
set startPosistion 0
set resultSize 5000

while {$count > 0} {
    set nodeList [proc_searchNodes $startPosistion $resultSize]
    set hasValue [java::isnull $nodeList]
    if {$hasValue == 1} {
    	set count 0
    	break
    } else {
    	puts "fetched content list 12"
    	set size [$nodeList size]
    	if {$size > 0} {
    		java::for {Node graph_node} $nodeList {
    			puts "updating content"
				set content_id [java::prop $graph_node "identifier"]
				set metadata [java::prop $graph_node "metadata"]
				set subject [$metadata get "subject"]
				set subjectVal_null [java::isnull $subject]
				set domain [$metadata get "domain"]
				set domainVal_null [java::isnull $domain]
				puts "checking domain and subject values"
				if {$subjectVal_null == 1 && $domainVal_null == 0} {
					set domainVal [proc_getFirstElement $domain]
					puts "first domain value is $domainVal"
					if {$domainVal == "numeracy"} {
						puts "setting subject to maths"
						$metadata put "subject" "MATHS"
					} else {
						puts "setting literacy subject"
						set language [$metadata get "language"]
						set languageVal [proc_getFirstElement $language]
						puts "first language value is $languageVal"
						$metadata put "subject" $languageVal
					}
					set update_response [updateDataNode $graph_id $content_id $graph_node]
					set check_error [check_response_error $update_response]
				    if {$check_error} {
				        set messages [get_resp_value $update_response "messages"]
				        java::for {String msg} $messages {
				            puts "$content_id - $msg"
				        }
				    }
				}
			}
    	} else {
    		set count 0
    		break
    	}
    }
    set startPosistion [expr {$startPosistion+5000}]
}
return "Updation Complete"