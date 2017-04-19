package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map


set request [java::new HashMap]
set new_filter [java::new HashMap]

set isFiltersNull [java::isnull $filters]
if {$isFiltersNull == 1} {
	set filters $new_filter
}
$filters put "objectType" "Content"

$request put "query" $query
$request put "filters" $filters
$request put "exists" $exists
$request put "not_exists" $not_exists
$request put "sort_by" $sort_by
$request put "facets" $facets
$request put "limit" $limit
$request put "offset" $offset
$request put "fields" $fields

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