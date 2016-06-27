package require java
java::import -package java.util HashMap Map
java::import -package java.util ArrayList List


set object_type "Content"
set graph_id "domain"
set search [java::new HashMap]
$search put "objectType" $object_type
$search put "nodeType" "DATA_NODE"
$search put "status" "Live"

set contentType [java::new ArrayList]
$contentType add "Story"
$contentType add "Worksheet"
$search put "contentType" $contentType

set search_criteria [create_search_criteria $search]
set search_response [searchNodes $graph_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set identifiers [java::new ArrayList]
	set graph_nodes [get_resp_value $search_response "node_list"]
	java::for {Node graph_node} $graph_nodes {
		set contentId [java::prop $graph_node "identifier"]
		$identifiers add $contentId
	}
	set result_map [java::new HashMap]
	$result_map put "identifiers" $identifiers
	set response_list [create_response $result_map]
	return $response_list
}
