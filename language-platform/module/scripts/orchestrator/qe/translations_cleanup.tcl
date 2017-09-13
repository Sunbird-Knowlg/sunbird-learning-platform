package require java
java::import -package java.util ArrayList List
java::import -package java.util HashSet Set
java::import -package java.util Arrays


proc checkValidWord {word startUnicode endUnicode} {
	
	set firstLetter [string index $word 0]
  	set i [scan $firstLetter %c]

  	puts "i $i"
	#char firstLetter = word.charAt(0);
	#int i = firstLetter;
	
	#String uc = String.format("%04x", i);
	#int hexVal = Integer.parseInt(uc, 16);
	set iVal [java::new Integer $i]
	puts "iVal $iVal"
	set obj [java::cast Object $iVal]
	set list [java::new ArrayList]
	$list add $obj
	set arr [$list toArray [java::new Object[[$list size]]]]
	#set arr [java::new Integer[]{$iVal}]
	set uc [java::call String format "%04x" $arr ]
	puts "uc $uc"
	set hexVal [java::call Integer parseInt $uc 16]
	puts "hexVal $hexVal"
	#int min = Integer.parseInt(startUnicode, 16);
	#int max = Integer.parseInt(endUnicode, 16);
	
	set min [java::call Integer parseInt $startUnicode 16]
	puts "min $min"
	
	set max [java::call Integer parseInt $endUnicode 16]
	puts "max $max"

	if {$hexVal >= $min && $hexVal <= $max} {
		return true
	} else {
		return false
	}			
}

puts "$word $startUnicode $endUnicode"
if {[checkValidWord $word $startUnicode $endUnicode]}
return "valid"
else
return "not valid"