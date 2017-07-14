package require java
java::import -package java.util HashMap Map
java::import -package java.util ArrayList List

proc proc_isNotNull {value} {
	set exist false
	java::try {
		set hasValue [java::isnull $value]
		if {$hasValue == 0} {
			set exist true
		}
	} catch {Exception err} {
    	set exist false
	}
	return $exist
}

proc proc_getFirstElement {input_list} {
	set listNotNull [proc_isNotNull $input_list]
	if {$listNotNull} {
		set arr_instance [java::instanceof $input_list {String[]}]
		if {$arr_instance == 1} {
			set array [java::cast {String[]} $input_list]
			set listSize [$array length]
			if {$listSize > 0} {
				set word [$array get 0]
				return $word
			} else {
				return [java::new String "English"]
			}
		} else {
			return [$input_list toString]
		}
	} else {
		return [java::new String "English"]
	}
}

proc proc_updateLanguageCode {resp_object graph_node} {
	set objectNotNull [proc_isNotNull $resp_object]
	if {$objectNotNull} {
		set languageCode [$resp_object get "languageCode"]
		set languageCodeNotNull [proc_isNotNull $languageCode]
		if {!$languageCodeNotNull} {
			set node_metadata [java::prop $graph_node "metadata"]
			set language [$node_metadata get "language"]
			set languageVal [proc_getFirstElement $language]
			set language_map [java::new HashMap]
			$language_map put "english" "en"
			$language_map put "telugu" "te"
			$language_map put "hindi" "hi"
			$language_map put "kannada" "ka"
			$language_map put "tamil" "ta"
			$language_map put "marathi" "mr"
			$language_map put "bengali" "bn"
			$language_map put "gujarati" "gu"
			$language_map put "odia" "or"
			$language_map put "assamese" "as"
			set lang_lc [[java::new String $languageVal] toLowerCase]
			set langCode [$language_map get $lang_lc]
			set langCodeNotNull [proc_isNotNull $langCode]
			if {$langCodeNotNull} {
				$resp_object put "languageCode" $langCode
			} else {
				$resp_object put "languageCode" "en"
			}
		}
	}
}

set isEditMode 0
set imageMode 0
set object_type "Content"
set graph_id "domain"
set content_image_id ${content_id}.img
set is_mode_null [java::isnull $mode]
set resp_get_node [java::null]]
if {($is_mode_null == 0) && ([$mode toString] == "edit")} {
	set resp_get_node [getDataNode $graph_id $content_image_id]
	set check_error [check_response_error $resp_get_node]
	if {$check_error} {
		set isEditMode 1
		set resp_get_node [getDataNode $graph_id $content_id]
	} else {
		set imageMode 1
	}
} else {
	set resp_get_node [getDataNode $graph_id $content_id]
}
set check_error [check_response_error $resp_get_node]
if {$check_error} {
	return $resp_get_node;
} else {
	set returnFields false
	set is_fieldList_null [java::isnull $fields]
	if {$is_fieldList_null == 0} {
		set returnFields true
	}
	set graph_node [get_resp_value $resp_get_node "node"]
	set metadata [java::prop $graph_node "metadata"]
        set status_val [$metadata get "status"]
        set status_val_str [java::new String [$status_val toString]]
        set isLiveState [$status_val_str equalsIgnoreCase "Live"]
	set isFlaggedState [$status_val_str equalsIgnoreCase "Flagged"]
	set isRetiredState [$status_val_str equalsIgnoreCase "FlRetiredagged"]
	set resp_def_node [getDefinition $graph_id $object_type]
	set def_node [get_resp_value $resp_def_node "definition_node"]
	if {$returnFields} {
		set resp_object [convert_graph_node $graph_node $def_node $fields]
		set externalProps [java::new ArrayList]
		set returnBody [$fields contains "body"]
		if {$returnBody == 1} {
			$externalProps add "body"
		}
		set returnOldBody [$fields contains "oldBody"]
		if {$returnOldBody == 1} {
			$externalProps add "oldBody"
		}
		set returnStageIcons [$fields contains "stageIcons"]
		if {$returnStageIcons == 1} {
			$externalProps add "stageIcons"
		}
		set externalPropId $content_id
		if {$imageMode == 1} {
			set externalPropId $content_image_id
		}
		set bodyResponse [getContentProperties $externalPropId $externalProps]
		set check_error [check_response_error $bodyResponse]
		if {!$check_error} {
			set extValues [get_resp_value $bodyResponse "values"]
			set is_extValues_null [java::isnull $extValues]
			if {$is_extValues_null == 0} {
				set extValuesMap [java::cast Map $extValues]
				$resp_object putAll $extValuesMap
			}
		}
	} else {
		set resp_object [convert_graph_node $graph_node $def_node]
	}
	proc_updateLanguageCode $resp_object $graph_node
	$resp_object put "identifier" $content_id

	if {$isEditMode == 1 && $isLiveState == 1} {
		$resp_object put "status" "Draft"
	}
	if ($isRetiredState == 1) {
		$resp_object put "status" "Draft"	
	}
   	
	set result_map [java::new HashMap]
	$result_map put "content" $resp_object
	set response_list [create_response $result_map]
	return $response_list
}
