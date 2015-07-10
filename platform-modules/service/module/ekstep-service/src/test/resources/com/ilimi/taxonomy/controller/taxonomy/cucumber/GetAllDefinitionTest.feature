Feature: Test all scenarios of get all definition.

  Scenario: Get all definition using taxonomy .
    When taxonomy ID is numeracy
    Then I should get all definition and status is successful
    
  Scenario: Get all definition using wrong taxonomy .
    When taxonomy ID is ilimi
    Then I should get Failed to get definition node and status should be 404