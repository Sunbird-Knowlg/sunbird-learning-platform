Feature: Test all scenarios of get concept.

  Scenario: Get a concept using identifier.
    When I give Taxonomy NUMERACY and Concept ID Num:C1
    Then I should get the concept with name Geometry
    
  Scenario: Get a concept using identifier with empty taxonomy Id.
    When I give Taxonomy  and Concept ID Num:C1
    Then I should get the status as 400
    
  Scenario: Get a concept using identifier.
    When I give Concept ID Num:C1
    Then I should get the status as 400
    
  Scenario: Get a concept using identifier.
    When I give Taxonomy NUMERACY and Concept ID jeetu
    Then I should get the status as 404
	
	    
