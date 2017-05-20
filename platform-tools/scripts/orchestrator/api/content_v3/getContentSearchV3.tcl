package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map


set request [java::new HashMap]
set filters [java::new HashMap]

$filters put "objectType" "Content"
$filters put "identifier" $content_id
set isModeEdit 0
set is_mode_null [java::isnull $mode]
if {($is_mode_null == 0) && ([$mode equalsIgnoreCase "edit"])} {
	set isModeEdit 1
}
if {$isModeEdit == 1} {
	set statusList [java::new ArrayList]
	$statusList add "Draft"
	$statusList add "Review"
	$statusList add "Flagged"
	$statusList add "Retired"
	$statusList add "Mock"
	$statusList add "Processing"
	$statusList add "FlagDraft"
	$statusList add "FlagReview"
	$statusList add "Failed"
	$filters put "status" $statusList
}
$request put "fields" $fields
$request put "filters" $filters

set compositeSearchResp [compositeSearchWrapper $request]

set check_error [check_response_error $compositeSearchResp]
if {$check_error} {
	return $compositeSearchResp;
}

set resultMap [java::prop $compositeSearchResp "result"]

set contentResult [$resultMap get "results"]
$resultMap put "content" $contentResult
$resultMap remove "results"

return $compositeSearchResp
