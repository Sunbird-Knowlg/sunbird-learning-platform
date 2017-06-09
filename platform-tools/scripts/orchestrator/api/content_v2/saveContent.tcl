package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util HashSet Set
java::import -package com.ilimi.graph.dac.model Node Relation
java::import -package com.ilimi.graph.model.node MetadataDefinition

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

set object_null [java::isnull $content]
if {$object_null == 1} {
	set result_map [java::new HashMap]
	$result_map put "code" "ERR_CONTENT_INVALID_OBJECT"
	$result_map put "message" "Invalid Request"
	$result_map put "responseCode" [java::new Integer 400]
	set response_list [create_error_response $result_map]
	return $response_list
} else {
	set object_type "Content"
	set graph_id "domain"
	set resp_def_node [getDefinition $graph_id $object_type]
	set def_node [get_resp_value $resp_def_node "definition_node"]
	$content put "objectType" $object_type

	set mimeType [$content get "mimeType"]
	set mimeTypeEmpty [proc_isEmpty $mimeType]
	if {!$mimeTypeEmpty} {
		set isApkMimeType [[java::new String [$mimeType toString]] equalsIgnoreCase "application/vnd.android.package-archive"]
		if {$isApkMimeType != 1} {
			$content put "osId" "org.ekstep.quiz.app"
		}
	}

	set osId_Error false
	set contentType [$content get "contentType"]
	set contentTypeEmpty [proc_isEmpty $contentType]
	if {!$mimeTypeEmpty} {
		set osId [$content get "osId"]
		set osIdEmpty [proc_isEmpty $osId]
		set osIdCheck [[java::new String [$mimeType toString]] equalsIgnoreCase "application/vnd.android.package-archive"]
		if {$osIdCheck == 1 && $osIdEmpty} {
			set osId_Error false
		}

		if {!$contentTypeEmpty} {
			set textbookUnitCheck [[java::new String [$contentType toString]] equalsIgnoreCase "TextBookUnit"]
			if {$textbookUnitCheck == 1} {
				$content put "visibility" "Parent"
			}
		}

		if {$osId_Error} {
			set result_map [java::new HashMap]
			$result_map put "code" "ERR_CONTENT_INVALID_OSID"
			$result_map put "message" "OSId cannot be empty"
			$result_map put "responseCode" [java::new Integer 400]
			set response_list [create_error_response $result_map]
			return $response_list
		} else {
			set externalProps [java::new HashMap]
			set body [$content get "body"]
			set bodyEmpty [proc_isEmpty $body]
			if {!$bodyEmpty} {
				$content put "body" [java::null]
				$externalProps put "body" $body
			}
			set oldBody [$content get "oldBody"]
			set oldBodyEmpty [proc_isEmpty $oldBody]
			if {!$oldBodyEmpty} {
				$content put "oldBody" [java::null]
				$externalProps put "oldBody" $oldBody
			}
			set stageIcons [$content get "stageIcons"]
			set stageIconsEmpty [proc_isEmpty $stageIcons]
			if {!$stageIconsEmpty} {
				$content put "stageIcons" [java::null]
				$externalProps put "stageIcons" $stageIcons
			}

			set codeValidationFailed 0
			if {!$mimeTypeEmpty} {
				set isPluginMimeType [[java::new String [$mimeType toString]] equalsIgnoreCase "application/vnd.ekstep.plugin-archive"]
				if {$isPluginMimeType == 1} {
					set pluginCode [$content get "code"]
					set codeEmpty [proc_isEmpty $pluginCode]
					if {$codeEmpty} {
						set codeValidationFailed 1
					} else {
						$content put "identifier" $pluginCode
					}
				}
			}
			if {$codeValidationFailed == 1} {
				set result_map [java::new HashMap]
				$result_map put "code" "ERR_PLUGIN_CODE_REQUIRED"
				$result_map put "message" "Unique code is mandatory for plugins"
				$result_map put "responseCode" [java::new Integer 400]
				set response_list [create_error_response $result_map]
				return $response_list
			} else {
				set domain_obj [convert_to_graph_node $content $def_node]
				set create_response [createDataNode $graph_id $domain_obj]
				set check_error [check_response_error $create_response]
				if {$check_error} {
					return $create_response
				} else {
					set content_id [get_resp_value $create_response "node_id"]
					if {!$bodyEmpty || !$oldBodyEmpty} {
						set bodyResponse [updateContentProperties $content_id $externalProps]
						set check_error [check_response_error $bodyResponse]
						if {$check_error} {
							return $bodyResponse
						} else {
							return $create_response
						}
					} else {
						return $create_response
					}
				}
			}
		}
	} else {
		set result_map [java::new HashMap]
		$result_map put "code" "ERR_CONTENT_INVALID_CONTENT_MIMETYPE_TYPE"
		$result_map put "message" "Mime Type cannot be empty"
		$result_map put "responseCode" [java::new Integer 400]
		set response_list [create_error_response $result_map]
		return $response_list
	}
}
