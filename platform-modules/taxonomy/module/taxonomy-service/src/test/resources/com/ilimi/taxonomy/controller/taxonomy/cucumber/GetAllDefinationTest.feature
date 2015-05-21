Feature: Test all scenarios of get all Defination.

  Scenario: Get all Defination using taxonomy .
    When taxonomy Id is NUMERACY
    Then I should get all defination and status is SUCCESS
    
  Scenario: Get all Defination using wrong taxonomy .
    When taxonomy Id is Num
    Then I should get Failed to get definition node and status should be 404