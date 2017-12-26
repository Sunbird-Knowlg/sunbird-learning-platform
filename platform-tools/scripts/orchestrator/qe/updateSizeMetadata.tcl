package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node
java::import -package org.ekstep.taxonomy.util AWSUploader

set object_type "Content"
set graph_id "domain"
set search [java::new HashMap]
$search put "objectType" $object_type
$search put "nodeType" "DATA_NODE"
$search put "status" "Live"

set contentType [java::new ArrayList]
$contentType add "Story"
$contentType add "Worksheet"
$contentType add "Collection"
$search put "contentType" $contentType

proc proc_getS3Key {metadata} {
	set s3key [java::null]
	set downloadUrl [$metadata get "downloadUrl"]
	set isURLNull [java::isnull $downloadUrl]
	if {$isURLNull == 0} {
		set urlStr [java::new String [$downloadUrl toString]]
		set index [$urlStr indexOf "amazonaws.com/"]
		set index [expr {$index+14}]
		set s3key [java::new String [$urlStr substring $index]]
		set checkKey [$s3key startsWith "ekstep-public/"]
		if {$checkKey} {
			set s3key [java::new String [$s3key substring [java::new Integer 14]]]
		}
	}
	return $s3key
}

set search_criteria [create_search_criteria $search]
set search_response [searchNodes $graph_id $search_criteria]
set check_error [check_response_error $search_response]
if {$check_error} {
	return $search_response;
} else {
	set graph_nodes [get_resp_value $search_response "node_list"]
	java::for {Node graph_node} $graph_nodes {
		set contentId [java::prop $graph_node "identifier"]
		set metadata [java::prop $graph_node "metadata"]
		set s3key [$metadata get "s3Key"]
		set isS3KeyNull [java::isnull $s3key]
		if {$isS3KeyNull == 1} {
			set s3key [proc_getS3Key $metadata]
			set isS3KeyNull [java::isnull $s3key]
		}
		if {$isS3KeyNull == 0} {
			set s3keyStr [$s3key toString]			
			java::try {
				set size [java::call AWSUploader "getObjectSize" "ekstep-public" $s3keyStr]
				$metadata put "size" [java::new Double $size]
				set update_response [updateDataNode $graph_id $contentId $graph_node]
				set check_error [check_response_error $update_response]
				if {$check_error} {
					set messages [get_resp_value $update_response "messages"]
					java::for {String msg} $messages {
						puts "$msg"
					}
				} else {

				}
			} catch {Exception err} {
    			puts "error getting $contentId size: $err"				
			}
		}
	}
}
return "OK"