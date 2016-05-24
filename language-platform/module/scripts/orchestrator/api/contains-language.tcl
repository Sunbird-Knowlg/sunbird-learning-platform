package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map

set language_response [getLanguages]
set check_error [check_response_error $language_response]
if {$check_error} {
	return $language_response
} else {
set languages [get_resp_value $language_response "languages"]
set lang_found false
java::for {Map map} $languages {
	set lang_code [$map get "code"]
	set lang_eqs [$lang_code equals $language_id]
	if {$lang_eqs} {
		set lang_found true
	}
}
return $lang_found
}