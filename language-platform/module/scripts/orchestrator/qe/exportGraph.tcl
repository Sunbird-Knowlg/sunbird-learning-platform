package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node
java::import -package org.ekstep.graph.importer OutputStreamValue
java::import -package java.io ByteArrayOutputStream

set search_criteria $search_criteria
set format "CSV"

set export_response [exportGraph $language_id $format $search_criteria]
set check_error [check_response_error $export_response]
if {$check_error} {
	return $export_response;
} else {
	set osValue [get_resp_value $export_response "output_stream"]
	set output_stream [$osValue "getOutputStream"]
	set baos [java::cast ByteArrayOutputStream $output_stream]
	set res_string [$baos toString]
	return $res_string
}