Feature: Test all scenarios of delete Relation Game.

  Scenario: Delete a Game using identifier.
  	When I give Game Id G1 Relation isParentOf with Game Id G1:M1 and taxonomy Id numeracy
    Then Delete the Relation and get the status successful
    
  Scenario: Delete a Game using identifier.
  	When I give Game Id G1 Relation isParentOf with Game Id G1:M1 and taxonomy Id empty
    Then I will get ErrMsg Taxonomy Id is blank and status is 400
    
  Scenario: Delete a Game using identifier.
  	When I give Game Id G1 Relation isParentOf with Game Id G1:M1 and taxonomy Id absent
    Then I will get ErrMsg Taxonomy Id is Required and status is 400
    
  Scenario: Delete a Game using identifier.
  	When I give Game Id ilimi Relation isParentOf with Game Id G1:M1 and taxonomy Id numeracy
    Then I will get ErrMsg Node not found and status is 404
    
  Scenario: Delete a concept using identifier.
  	When I give Game Id G1 Relation associated with Game Id G1:M1 and taxonomy Id numeracy
    Then I will get unsupported relation and status is 400