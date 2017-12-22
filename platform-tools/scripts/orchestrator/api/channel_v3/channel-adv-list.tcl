
package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package org.ekstep.graph.dac.model Node

proc proc_isEmpty {value} {
	set exist false
	java::try {
		set hasValue [java::isnull $value]
		if {$hasValue == 1} {
			set exist true
		} else {
			set strValue [$value toString]
			set newStrValue [java::new String $strValue]
			set strLength [$newStrValue length]
			if {$strLength == 0} {
				set exist true
			}
		}
	} catch {Exception err} {
    	set exist true
	}
	return $exist
}

set object_type "Channel"
set graph_id "domain"

set resultMaps [java::new HashMap]
set values [java::new ArrayList]
set result_map [java::new HashMap]
set get_node_response [getAllObjects $graph_id $object_type]
set channel_object [get_resp_value $get_node_response "object_list"]
      java::for {Map object} $channel_object {
		set resultMap $object
		set label [$resultMap get "name"]
		set labelData [$label toString]
		
		set value [$resultMap get "identifier"]
		set valueData [$value toString]

		set telemetry $valueData
		set search [$resultMap get "contentFilter"]
		set searchData [$search toString]
		
		$result_map put "label" $labelData
		$result_map put "value" $valueData
		$result_map put "telemetry" $telemetry
		$result_map put "search" $search
        	$values add $result_map
                $resultMaps put "label" "Channel"
        	$resultMaps put "value" "channel"
        	$resultMaps put "language" "en"
        	$resultMaps put "description" "List of partner channels"
                $resultMaps put "values" $values

	}
set channel_response [create_response $resultMaps]
return $channel_response

