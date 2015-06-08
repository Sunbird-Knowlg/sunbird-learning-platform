Feature: Test all scenarios of get definition.

  Scenario: Get definition using taxonomy and objectType.
    When taxonomy Id is numeracy and objectType is Game
    Then I should get the numeracy and status is successful
    
 Scenario: Get definition using objectType and wrong taxonomy.
    When taxonomy Id is Num and objectType is Game
    Then I should get status is 404 and error message Failed to get definition node
    
 Scenario: Get definition using using taxonomy and wrong objectType.
    When taxonomy Id is numeracy and objectType is wrongGame
    Then I should get status is 404 and error message Failed to get definition node