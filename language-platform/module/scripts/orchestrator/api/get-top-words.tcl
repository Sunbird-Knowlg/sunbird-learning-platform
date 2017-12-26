package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map
java::import -package java.util Calendar Date
java::import -package org.ekstep.graph.common DateUtils
java::import -package org.ekstep.graph.dac.model Node Relation

set filters [java::new HashMap]
$filters put "objectType" "Word"
$filters put "graph_id" $language_id

set statusList [java::new ArrayList]
$statusList add "Live"
$statusList add "Draft"
$filters put "status" $statusList

set calendar [java::call Calendar getInstance]
$calendar add [java::field Calendar DAY_OF_MONTH] [java::new Integer -2]
set dateValue [$calendar getTime]
set dateStr [java::call DateUtils format $dateValue]
set dateRange [java::new HashMap]
$dateRange put "max" $dateStr
$filters put "lastUpdatedOn" $dateRange

set limit [java::new Integer 5]

set exists [java::new ArrayList]
$exists add "primaryMeaningId"

set notExists [java::new ArrayList]
$notExists add "pictures"
$notExists add "pronunciations"

set sortMap [java::new HashMap]
$sortMap put "lastUpdatedOn" "desc"

set null_var [java::null]
set empty_list [java::new ArrayList]
set empty_map [java::new HashMap]

set searchResponse [indexSearch $null_var $null_var $filters $exists $notExists $sortMap $empty_list $null_var $null_var $limit]
set searchResultsMap [$searchResponse getResult]
set compositeSearchResponse [groupSearchResultByObjectType $searchResultsMap]
return $compositeSearchResponse




