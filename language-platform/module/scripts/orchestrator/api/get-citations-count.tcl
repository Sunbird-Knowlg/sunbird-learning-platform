package require java
java::import -package java.util ArrayList List
java::import -package java.util HashMap Map

set language_response [lang_getCitationsCount]
return $language_response