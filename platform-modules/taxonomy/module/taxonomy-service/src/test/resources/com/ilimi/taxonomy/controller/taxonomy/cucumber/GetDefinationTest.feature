Feature: Test all scenarios of get Defination.

  Scenario: Get Defination using taxonomy and objectType.
    When taxonomy Id is NUMERACY and objectType is Game
    Then I should get the NUMERACY and status is SUCCESS
    
 Scenario: Get Defination using objectType and wrong taxonomy.
    When taxonomy Id is Num and objectType is Game
    Then I should get status is 404 and error message Failed to get definition node
    
 Scenario: Get Defination using using taxonomy and wrong objectType.
    When taxonomy Id is NUMERACY and objectType is wrongGame
    Then I should get status is 404 and error message Failed to get definition node