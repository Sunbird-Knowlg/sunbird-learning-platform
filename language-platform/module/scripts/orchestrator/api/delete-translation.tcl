package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package com.ilimi.graph.dac.model Node

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

proc getProperty {graph_node prop} {
	set property [java::prop $graph_node $prop]
	return $property
}

set object_type "TranslationSet"
set node_id $word_id
set language_id $language_id
set synset_list [java::new ArrayList]

set testMap [java::cast HashMap $translations]
puts "testing"

java::for {String translationKey} [$translations keySet] {
	puts "testing"
	$synset_list add $translationKey
    set testMap [java::cast HashMap [$translations get $translationKey]]
	java::for {String language} [$testMap keySet] {
		set synsetList [java::cast List [$testMap get $language]]
		$synset_list addAll $synsetList
	}

	set relationMap [java::new HashMap]
	$relationMap put "name" "hasMembers"
	$relationMap put "objectType" "Synset"
	$relationMap put "identifiers" $synset_list

	set criteria_list [java::new ArrayList]
	$criteria_list add $relationMap

	set criteria_map [java::new HashMap]
	$criteria_map put "nodeType" "SET"
	$criteria_map put "objectType" $object_type
	$criteria_map put "relationCriteria" $criteria_list

	set graph_id "translations"
	set collection_type "SET"

	set search_criteria [create_search_criteria $criteria_map]
	set search_response [searchNodes $graph_id $search_criteria]
	set check_error [check_response_error $search_response]
	if {$check_error} {
		return $search_response;
	} else {
		set result_map [java::new HashMap]
		java::try {
			set graph_nodes [get_resp_value $search_response "node_list"]
			set translationExists [isNotEmpty $graph_nodes]
			if {$translationExists} {			
			java::for {Node graph_node} $graph_nodes {
					set collection_id [getProperty $graph_node "identifier"]
					set synset_ids [getNodeRelationIds $graph_node "Synset" "endNodeId"]
					set not_empty_list [isNotEmpty $synset_ids]
					if {$not_empty_list} {
					set members [java::new ArrayList]
					java::for {java::new String synsetId} $synset_list {
						set synsetContains [$synset_ids contains synsetId]
						if {synsetContains}{
							$members add synsetId					
						}
					}
					set searchResponse [removeMembers $graph_id $collection_id $collection_type $members]
					}
				}
			} 
		} catch {Exception err} {
				$result_map put "error" [$err getMessage]
			}
		}
}

set get_node_response [getDataNode $language_id $node_id]
set get_node_response_error [check_response_error $get_node_response]
if {$get_node_response_error} {
	return $get_node_response
}

set word_node [get_resp_value $get_node_response "node"]
set eventResp [log_translation_lifecycle_event $word_id $word_node]

return $searchResponse

