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

set compositeSearchResp [compositeSearch $request]
return $compositeSearchResp