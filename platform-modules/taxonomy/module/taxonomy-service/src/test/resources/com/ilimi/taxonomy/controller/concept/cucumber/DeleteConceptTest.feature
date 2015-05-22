Feature: Test all scenarios of delete concept.

  Scenario: Delete a concept using identifier.
  	When I Give Taxonomy ID numeracy and Concept ID Num:C1
    Then Delete the Concept Id Num:C1
    
  Scenario: Delete a concept using identifier with empty taxonomy id.
  	When I Give Taxonomy ID empty and Concept ID Num:C1
    Then I should get errMsg taxonomy id is blank and status is 400
    
  Scenario: Delete a concept using identifier without taxonomy id.
  	When I Give Taxonomy ID absent and Concept ID Num:C1
    Then I should get errMsg taxonomy id is Required and status is 400
    
  Scenario: Delete a concept using wrong identifier.
  	When I Give Taxonomy ID numeracy and Concept ID ilimi
    Then I should get errMsg Node not found and status is 404
    
    
    
    
    
  
