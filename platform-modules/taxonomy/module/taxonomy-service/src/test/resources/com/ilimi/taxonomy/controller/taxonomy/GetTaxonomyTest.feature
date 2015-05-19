Feature: Test all scenarios of get Taxonomy.

  Scenario: Get Taxonomy using identifier.
    When taxonomy Id is NUMERACY and Game Id is G1
    Then i should get the Game with status successful
    
  Scenario: Get Taxonomy using identifier when .
    When taxonomy Id is empty and Game Id is G1
    Then i should get Error message Taxonomy Id is blank and status is 400
    
  Scenario: Get a Game using identifier.
    When taxonomy Id is absent and Game Id is G1
    Then i should get Error message Taxonomy Id is Requried and status is 400