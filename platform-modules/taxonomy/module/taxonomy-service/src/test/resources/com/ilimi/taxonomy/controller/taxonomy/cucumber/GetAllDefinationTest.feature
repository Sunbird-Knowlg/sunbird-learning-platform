Feature: Test all scenarios of get all Defination.

  Scenario: Get all Defination using taxonomy .
    When taxonomy ID is numeracy
    Then I should get all defination and status is successful
    
  Scenario: Get all Defination using wrong taxonomy .
    When taxonomy ID is ilimi
    Then I should get Failed to get definition node and status should be 404