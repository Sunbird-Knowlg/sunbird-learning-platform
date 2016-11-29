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

set object_type "Content"
set graph_id "domain"
set resp_get_node [getDataNode $graph_id $content_id]
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
	set resp_def_node [getDefinition $graph_id $object_type]
	set def_node [get_resp_value $resp_def_node "definition_node"]
	if {$returnFields} {
		set resp_object [convert_graph_node $graph_node $def_node $fields]
		set returnBody [$fields contains "body"]
		if {$returnBody == 1} {
			set bodyResponse [getContentBody $content_id]
			set body [get_resp_value $bodyResponse "body"]
			$resp_object put "body" $body
		}
	} else {
		set resp_object [convert_graph_node $graph_node $def_node]
	}
	proc_updateLanguageCode $resp_object $graph_node
	set result_map [java::new HashMap]
	$result_map put "content" $resp_object
	set response_list [create_response $result_map]
	return $response_list
}