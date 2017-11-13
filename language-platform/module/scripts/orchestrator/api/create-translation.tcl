package require java
package require json
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node Relation
java::import -package java.util HashSet Set

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

proc getSynsetLanguage {synset} {

	set languageId ""
	if {[string match *:* $synset] == 1} {
		#if synset contains :, then extract language_id by splitting and getting its languageId from 0th position
		set languageId [lindex [split $synset ":"] 0]
	} elseif {[string match *_* $synset] == 1} {
		#if synset contains _, then extract language_id by splitting and getting its languageId from 0th position
		set languageId [lindex [split $synset "_"] 0]
	} else {
		#if synset does not contain either : or _, then assume it is en
		set languageId "en"
	}

	return $languageId
}

proc checkOnlyOneSynsetPerLanguageExist {synsetList} {

	set synsetList [java::cast List $synsetList]
	set synsets [java::new HashSet $synsetList] 
	puts "synsets [$synsets toString]"
	set languageIds [java::new ArrayList]

	java::for {String synset} $synsets {
		puts "synset $synset"
		set synsetLanguageId [getSynsetLanguage $synset]
		if {![$languageIds contains $synsetLanguageId]} {
			$languageIds add $synsetLanguageId
		} else {
			return false
		}
		
	}
	return true
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
logs "translations [$translations toString] , word_id $word_id, language_id $language_id"

set set_list [java::new ArrayList]
set exists false
java::for {String translationKey} [$translations keySet] {
	$synset_list add $translationKey
    set testMap [java::cast HashMap [$translations get $translationKey]]
	java::for {String language} [$testMap keySet] {
		set synsetList [java::cast List [$testMap get $language]]
		set synsetListSize [$synsetList size]
		if {$synsetListSize > 1} {
			set msg "INVALID REQUEST FORMAT"
			set code "INVALID_REQUEST_FORMAT"
			set respCode 400
			return [getErrorResponse $msg $code $respCode]
		}
		$synset_list addAll $synsetList
	}
	set synsetListSize [$synset_list size]
	logs "translations word_id $word_id, language_id $language_id, synset_list [$synset_list toString]"
	if {$synsetListSize > 0} {
	
		if {$synsetListSize == 1} {
			set msg "INVALID REQUEST FORMAT"
			set code "INVALID_REQUEST_FORMAT"
			set respCode 400
			return [getErrorResponse $msg $code $respCode]
		}
		
		set synsetResp [multiLanguageSynsetSearch $synset_list]
		set synsetIds [java::cast List [$synsetResp get "synsets"]]
		logs "translations word_id $word_id, language_id $language_id, synsetIds [$synsetIds toString]"
		java::for {String synsetEntry} $synset_list {
			if {![$synsetIds contains $synsetEntry]} {
				set msg "One Synset per Language can only be linked"
				set code "ERR_SYNSET_EXIST"
				set respCode 400
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
		logs "translations word_id $word_id, language_id $language_id, graph_synset_list [$graph_synset_list toString]"
		java::for {String synset_id} $synset_list {
			if {![$graph_synset_list contains $synset_id]} {
				set proxy_node [java::new Node $synset_id "PROXY_NODE" $proxyType]
				set metadata [java::new HashMap]
				$metadata put "graphId" $graph_id
				$proxy_node setMetadata $metadata
				logs "translations proxy_node , word_id $word_id, language_id $language_id, synset_id $synset_id, metadata [$metadata toString]"
				set create_response [createProxyNode $graph_id $proxy_node]
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
		}
		 else {
				set graph_nodes [get_resp_value $search_response "node_list"]
				set translationExists [isNotEmpty $graph_nodes]
				logs "translations word_id $word_id, language_id $language_id, translationExists $translationExists"
				if {$translationExists} {
					set translationSize [$graph_nodes size] 
				
					set graph_node [java::cast Node [$graph_nodes get 0]]
					set collection_id [getProperty $graph_node "identifier"]
					
					set collection_type "SET"
					set synset_ids [getNodeRelationIds $graph_node "Synset" "endNodeId"]
					set not_empty_list [isNotEmpty $synset_ids]
					logs "translations word_id $word_id, language_id $language_id, not_empty_list $not_empty_list"
					if {$not_empty_list} {
						set members [java::new ArrayList]
						java::for {String synsetId} $synset_list {
							set synsetContains [$synset_ids contains $synsetId]
							if {!$synsetContains} {
								$members add $synsetId						
							}
						}
						set collectionIdsToBeDeleted [java::new ArrayList]
						if {$translationSize > 0} {
							java::for {Node graph_node} $graph_nodes {
								set collection_node_id [getProperty $graph_node "identifier"]
								if {$collection_node_id != $collection_id} {
									set synset_ids [getNodeRelationIds $graph_node "Synset" "endNodeId"]
									set not_empty_list [isNotEmpty $synset_ids]
									if {$not_empty_list} {
										$members addAll $synset_ids
									}
									#set dropResp [dropCollection $graph_id $collection_node_id $collection_type]
									$collectionIdsToBeDeleted add $collection_node_id
								}
							}
						}

						$synset_list addAll $members

						if {[checkOnlyOneSynsetPerLanguageExist $synset_list]} {
							java::for {String collection_node_id} $collectionIdsToBeDeleted {
								set dropResp [dropCollection $graph_id $collection_node_id $collection_type]
							}
						} else {
							set msg "One Synset per Language can only be linked"
							set code "ERR_SYNSET_EXIST"
							set respCode 400
							return [getErrorResponse $msg $code $respCode]
						}

						set membersSize [$members size] 
						if {$membersSize > 0} {
							logs "translations addMembers word_id $word_id, language_id $language_id, members [$members toString]"
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
					logs "translations createSet word_id $word_id, language_id $language_id, members [$members toString]"
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

set resultSize [$result_map size] 
set setListSize [$set_list size] 

if {$setListSize > 0} {
	logs "translations resultMap , word_id $word_id, language_id $language_id, set_list [$set_list toString]"
	$result_map put "set_list" $set_list
	set response_list [create_response $result_map]
	return $response_list
} else {
	return $searchResponse
}
