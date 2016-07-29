package require java
java::import -package java.util HashMap Map
java::import -package java.util ArrayList List
java::import -package com.ilimi.graph.dac.model Node
java::import -package com.ilimi.graph.common JSONUtils


proc proc_getOptionValue {valueMap index} {
	set text [$valueMap get "text"]
	set count [$valueMap get "count"]
	set textNull [java::isnull $text]
	set countNull [java::isnull $count]
	if {$textNull == 0 && [$text toString] != ""} {
		return [$text toString]
	} elseif {$countNull == 0 && [$count toString] != ""} {
		return [$count toString]
	} else {
		return $index
	}
}

proc proc_checkOptions {options} {
	set returnList [java::null]
	set isOptionsNull [java::isnull $options]
	if {$isOptionsNull == 0} {
		set optionsStr [$options toString]
		set update 0
		set jsonObj [java::call JSONUtils convertJSONString $optionsStr]
		set listInstance [java::instanceof $jsonObj List]
		if {$listInstance == 1} {
			set index 0
			set list [java::cast List $jsonObj]
			java::for {Object option} $list {
				set map [java::cast Map $option]
				set valueMap [java::cast Map [$map get "value"]]
				set type [[$valueMap get "type"] toString]
				if {$type == "mixed"} {
					set asset [$valueMap get "asset"]	
					set assetVal [proc_getOptionValue $valueMap $index]
					set isAssetNull [java::isnull $asset]
					if {$isAssetNull == 1 || [$asset toString] != $assetVal} {
						$valueMap put "asset" $assetVal
						set update 1
					}
				}
				set index [expr {$index+1}]
			}
			if {$update == 1} {
				set returnList [java::new ArrayList]
				$returnList addAll $list
			}
		}
	}
	return $returnList
}

set graph_id "domain"
set object_type "AssessmentItem"
set search [java::new HashMap]
$search put "objectType" $object_type
$search put "nodeType" "DATA_NODE"

set types [java::new ArrayList]
$types add "mcq"
$types add "mtf"
$search put "type" $types

set search_criteria [create_search_criteria $search]
set search_response [searchNodes $graph_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set graph_nodes [get_resp_value $search_response "node_list"]
	java::for {Node graph_node} $graph_nodes {
		set itemId [java::prop $graph_node "identifier"]
		set metadata [java::prop $graph_node "metadata"]
		set itemType [[$metadata get "type"] toString]
		if {$itemType == "mcq"} {
			set options [$metadata get "options"]	
			set list [proc_checkOptions $options]
			set isListNull [java::isnull $list]
			if {$isListNull == 0} {
				set node [java::new Node $itemId "DATA_NODE" "AssessmentItem"]
				set node_metadata [java::new HashMap]
				$node_metadata put "options" $list
				java::prop $node "metadata" $node_metadata
				set update_response [updateDataNode $graph_id $itemId $node]
				puts "$itemId - mcq item updated"
			}
		} elseif {$itemType == "mtf"} {
			set mtf_update 0
			set lhs_options [$metadata get "lhs_options"]	
			set rhs_options [$metadata get "rhs_options"]	
			set node [java::new Node $itemId "DATA_NODE" "AssessmentItem"]
			set node_metadata [java::new HashMap]
			java::prop $node "metadata" $node_metadata
			set lhs_list [proc_checkOptions $lhs_options]
			set isLHSNull [java::isnull $lhs_list]
			if {$isLHSNull == 0} {
				$node_metadata put "lhs_options" $lhs_list
				set mtf_update 1
			}
			set rhs_list [proc_checkOptions $rhs_options]
			set isRHSNull [java::isnull $rhs_list]
			if {$isRHSNull == 0} {
				$node_metadata put "rhs_options" $rhs_list
				set mtf_update 1
			}
			if {$mtf_update == 1} {
				set update_response [updateDataNode $graph_id $itemId $node]
				puts "$itemId - mtf item updated"
			}
		}
	}	
	return "ok"
}