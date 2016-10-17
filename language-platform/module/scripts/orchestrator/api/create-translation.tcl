package require java
package require json
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation

proc isNotEmpty {graph_nodes} {
	set exist false
	set hasRelations [java::isnull $graph_nodes]
	if {$hasRelations == 0} {
		set relationsSize [$graph_nodes size] 
		if {$relationsSize > 0} {
			set exist true
		}
	}
	return $exist
}

proc getOutRelations {graph_node} {
	set outRelations [java::prop $graph_node "outRelations"]
	return $outRelations
}

proc getNodeRelationIds {graph_node relationType property} {

	set relationIds [java::new ArrayList]
	set outRelations [getOutRelations $graph_node]
	set hasRelations [isNotEmpty $outRelations]
	if {$hasRelations} {
		java::for {Relation relation} $outRelations {
			if {[java::prop $relation "endNodeObjectType"] == $relationType} {
				set prop_value [java::prop $relation $property]
					$relationIds add $prop_value
				}				
			}
	}
	return $relationIds
}

proc getProperty {graph_node prop} {
	set property [java::prop $graph_node $prop]
	return $property
}

proc getErrorResponse {message code respCode} {
	set result_map [java::new HashMap]
	$result_map put "code" $code
	$result_map put "message" $message
	$result_map put "responseCode" [java::new Integer $respCode]
	set err_response [create_error_response $result_map]
	return $err_response
}

set object_type "TranslationSet"
set node_id $word_id
set language_id $language_id
set synset_list [java::new ArrayList]
set graph_synset_list [java::new ArrayList]
set proxyType "Synset"

set testMap [java::cast HashMap $translations]
set graph_id "translations"
set result_map [java::new HashMap]

set set_list [java::new ArrayList]
set exists false
java::for {String translationKey} [$translations keySet] {
	$synset_list add $translationKey
    set testMap [java::cast HashMap [$translations get $translationKey]]
	java::for {String language} [$testMap keySet] {
		set synsetList [java::cast List [$testMap get $language]]
		$synset_list addAll $synsetList
	}
	set synsetListSize [$synset_list size]
	
	if {$synsetListSize > 0} {
	
	if {$synsetListSize == 1} {
		set msg "INVALID REQUEST FORMAT"
		set code "INVALID_REQUEST_FORMAT"
		set respCode 400
		return [getErrorResponse $msg $code $respCode]
	}
	
	set synsetResp [multiLanguageSynsetSearch $synset_list]
	set synsetIds [java::cast List [$synsetResp get "synsets"]]
	java::for {String synsetEntry} $synset_list {
		if {![$synsetIds contains $synsetEntry]} {
			set msg "SYNSET NOT FOUND"
			set code "SYNSET_NOT_FOUND"
			set respCode 404
			return [getErrorResponse $msg $code $respCode]
		}
	}
	
	
	set proxyResp [getDataNodes $graph_id $synset_list]
	set proxy_nodes [get_resp_value $proxyResp "node_list"]
	set proxyExists [isNotEmpty $proxy_nodes]
	if {$proxyExists} {
		java::for {Node proxy_node} $proxy_nodes {
		set proxy_id [getProperty $proxy_node "identifier"]
		$graph_synset_list add $proxy_id
	}
	}
	
	java::for {String synset_id} $synset_list {
		if {![$graph_synset_list contains $synset_id]} {
			set resp_def_node [getDefinition $graph_id $proxyType]
			set def_node [get_resp_value $resp_def_node "definition_node"]
			set synsetMap [java::new HashMap]
			$synsetMap put "objectType" $proxyType
			$synsetMap put "graphId" $graph_id
			$synsetMap put "identifier" $synset_id
			set synset_obj [convert_to_graph_node $synsetMap $def_node]
			set create_response [createProxyNode $graph_id $synset_obj]
		}
	}
	
	set relationMap [java::new HashMap]
	$relationMap put "name" "hasMember"
	$relationMap put "objectType" "Synset"
	$relationMap put "identifiers" $synset_list

	set criteria_list [java::new ArrayList]
	$criteria_list add $relationMap

	set criteria_map [java::new HashMap]
	$criteria_map put "nodeType" "SET"
	$criteria_map put "objectType" $object_type
	$criteria_map put "relationCriteria" $criteria_list

	set search_criteria [create_search_criteria $criteria_map]
	set search_response [searchNodes $graph_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		return $search_response;
	} else {
			set graph_nodes [get_resp_value $search_response "node_list"]
			set translationExists [isNotEmpty $graph_nodes]
			if {$translationExists} {
			set translationSize [$graph_nodes size] 
			
				set graph_node [java::cast Node [$graph_nodes get 0]]
				set collection_id [getProperty $graph_node "identifier"]
				
				set collection_type "SET"
				set synset_ids [getNodeRelationIds $graph_node "Synset" "endNodeId"]
				set not_empty_list [isNotEmpty $synset_ids]
				if {$not_empty_list} {
					set members [java::new ArrayList]
					java::for {String synsetId} $synset_list {
						set synsetContains [$synset_ids contains $synsetId]
						if {!$synsetContains} {
							$members add $synsetId						
						}
					}
					if {$translationSize > 0} {
						java::for {Node graph_node} $graph_nodes {
							set collection_node_id [getProperty $graph_node "identifier"]
							if {$collection_node_id != $collection_id} {
								set synset_ids [getNodeRelationIds $graph_node "Synset" "endNodeId"]
								set not_empty_list [isNotEmpty $synset_ids]
								if {$not_empty_list} {
									$members addAll $synset_ids
								}
								set dropResp [dropCollection $graph_id $collection_node_id $collection_type]
							}
						}
					}
					set membersSize [$members size] 
					if {$membersSize > 0} {
						set searchResponse [addMembers $graph_id $collection_id $collection_type $members]
					}
					$set_list add $collection_id
			}
				
			} else {
				set node [java::new Node]
				$node setObjectType "TranslationSet"
				set members [java::new ArrayList]
				$members addAll $synset_list
				set member_type "Synset"
				set searchResponse [createSet $graph_id $members $object_type $member_type $node]
				set set_id [get_resp_value $searchResponse "set_id"]
				$set_list add $set_id
			}

	}
	} else {
		set msg "INVALID REQUEST FORMAT"
		set code "INVALID_REQUEST_FORMAT"
		set respCode 400
		return [getErrorResponse $msg $code $respCode]
	}
}

set get_node_response [getDataNode $language_id $node_id]
set get_node_response_error [check_response_error $get_node_response]
if {$get_node_response_error} {
	return $get_node_response
}

set word_node [get_resp_value $get_node_response "node"]
set eventResp [log_translation_lifecycle_event $word_id $word_node]
set resultSize [$result_map size] 
set setListSize [$set_list size] 

if {$setListSize > 0} {
	$result_map put "set_list" $set_list
	set response_list [create_response $result_map]
	return $response_list
} else {
	return $searchResponse
}



