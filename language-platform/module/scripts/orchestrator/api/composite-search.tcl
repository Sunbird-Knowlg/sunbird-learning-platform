package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map

set null_var [java::null]
set empty_list [java::new ArrayList]
set empty_map [java::new HashMap]

set traversals [$request get "traversals"]
set traversalsNull [java::isnull $traversals]
if {$traversalsNull == 1} {
	set traversals $empty_list
}

set query [$request get "query"]
set queryNull [java::isnull $query]
if {$queryNull == 1} {
	set query $null_var
}
set filters [$request get "filters"]
set filtersNull [java::isnull $filters]
if {$filtersNull == 1} {
	set filters $empty_map
}

set exists [$request get "exists"]
set existsNull [java::isnull $exists]
if {$existsNull == 1} {
	set exists $empty_list
}

set not_exists [$request get "not_exists"]
set not_existsNull [java::isnull $not_exists]
if {$not_existsNull == 1} {
	set not_exists $empty_list
}

set sort_by [$request get "sort_by"]
set sort_byNull [java::isnull $sort_by]
if {$sort_byNull == 1} {
	set sort_by $empty_map
}

set facets [$request get "facets"]
set facetsNull [java::isnull $facets]
if {$facetsNull == 1} {
	set facets $empty_list
}

set fuzzy [$request get "fuzzy"]
set fuzzyNull [java::isnull $fuzzy]
if {$fuzzyNull == 1} {
	set fuzzy $null_var
}

set limit [$request get "limit"]
set limitNull [java::isnull $limit]
if {$limitNull == 1} {
	set limit [java::new Integer 10000]
}

set searchResponse [indexSearch $traversals $query $filters $exists $not_exists $sort_by $facets $fuzzy $limit]

return $searchResponse
