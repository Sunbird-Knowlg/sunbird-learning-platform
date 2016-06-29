package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node


proc procGetWordListMembers {getDataNodesResp} {
	set nodes [get_resp_value $getDataNodesResp "node_list"]
	set word_list [java::new ArrayList]
	java::for {Node graph_node} $nodes {
		set wordMetadataRes [lang_qe_getWordMetadata $graph_node]
		set wordMetadata [get_resp_value $wordMetadataRes "result"]
		$word_list add $wordMetadata
	}
	return $word_list
}

set object_type "WordList"

set map [java::new HashMap]
$map put "nodeType" "SET"
$map put "objectType" $object_type
$map put "code" $wordlist
set search_criteria [create_search_criteria $map]
set search_response [searchNodes $language_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set result_map [java::new HashMap]
	java::try {
		set graph_nodes [get_resp_value $search_response "node_list"]
		set list_size [$graph_nodes size]
		if {$list_size > 0} {
			set graph_node [$graph_nodes get 0]
			set graph_node_map [java::cast Node $graph_node]
			set wordlistId [java::prop $graph_node_map "identifier"]
			set membersResp [getSetMembers $language_id $wordlistId]
			set check_error [check_response_error $membersResp]
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
					set word_list [procGetWordListMembers $getDataNodesResp]
					$result_map put "words" $word_list
					set response_list [create_response $result_map]
					set response_csv [convert_response_to_csv $response_list "words"]
					return $response_csv
				}
			}
		} else {
			$result_map put "error" "WordList not found"
			set response_list [create_response $result_map]
			return $response_list
		}
	} catch {Exception err} {
    	$result_map put "error" [$err getMessage]
    	set response_list [create_response $result_map]
		return $response_list
	}
}