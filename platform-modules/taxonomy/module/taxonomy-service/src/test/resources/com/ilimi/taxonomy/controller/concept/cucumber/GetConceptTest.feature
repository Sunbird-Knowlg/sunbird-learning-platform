Feature: Test all scenarios of get concept.

  Scenario: Get a concept using identifier.
    When I give Taxonomy numeracy and Concept ID Num:C1
    Then I should get the concept with name Geometry
    
  Scenario: Get a concept using identifier with empty taxonomy Id.
    When I give Taxonomy empty and Concept ID Num:C1
    Then I should get the status as 400 and taxonomy id is blank
    
  Scenario: Get a concept using identifier.
    When I give Taxonomy absent and Concept ID Num:C1
    Then I should get the status as 400 and taxonomy id is Required
    
  Scenario: Get a concept using identifier.
    When I give Taxonomy numeracy and Concept ID ilimi
    Then I should get the status as 404 and node not found
	
	    
