Feature: Test all scenarios of delete concept.

  Scenario: Delete a concept using identifier.
  	When I give Concept id Num:C1 Relation isParentOf with Concept ID Num:C1:SC1 and taxonomy Id NUMERACY
    Then Delete the relation and get the status successful
    
  Scenario: Delete a concept using identifier.
  	When I give Concept id Num:C1 Relation isParentOf with Concept ID Num:C1:SC1 and taxonomy Id empty
    Then I should get ErrMsg Taxonomy Id is blank and status is 400
    
  Scenario: Delete a concept using identifier.
  	When I give Concept id Num:C1 Relation isParentOf with Concept ID Num:C1:SC1 and taxonomy Id absent
    Then I should get ErrMsg Taxonomy Id is Required and status is 400
    
  Scenario: Delete a concept using identifier.
  	When I give Concept id jeetu Relation isParentOf with Concept ID Num:C1:SC1 and taxonomy Id NUMERACY
    Then I should get ErrMsg Node not found and status is 404
    
  Scenario: Delete a concept using identifier.
  	When I give Concept id Num:C1 Relation isParent with Concept ID Num:C1:SC1 and taxonomy Id NUMERACY
    Then I should get unsupported relation and status is 400