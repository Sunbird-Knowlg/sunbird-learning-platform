Feature: Test all scenarios of delete concept.

  Scenario: Delete a concept using identifier.
  	When I Give Taxonomy ID NUMERACY and Concept ID Num:C1
    Then Delete the Concept Id Num:C1
    Then Delete the Concept Id Num:C1
  
