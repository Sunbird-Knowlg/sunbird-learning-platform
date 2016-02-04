package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map


proc isNotNull {value} {
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

set resp_word_info [lang_getWordsInformation $language_id $words]
set check_error [check_response_error $resp_word_info]
if {$check_error} {
	return $resp_word_info
} else {
	set word_info [get_resp_value $resp_word_info "word_info"]
	set word_info_list [java::new ArrayList]
	set word_info_not_null [isNotNull $word_info]
	if {$word_info_not_null} {
		set info_list [$word_info "values"]
		$word_info_list addAll $info_list
	}
	set result_map [java::new HashMap]
	$result_map put "words" $word_info_list
	set response_list [create_response $result_map]
	set response_csv [convert_response_to_csv $response_list "words"]
	return $response_csv
}
