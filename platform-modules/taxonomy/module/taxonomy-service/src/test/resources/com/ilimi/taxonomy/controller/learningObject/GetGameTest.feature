Feature: Test all scenarios of get Game.

  Scenario: Get a Game using identifier.
    When taxonomy id is NUMERACY and Game Id is G1
    Then i should get the Game with status SUCCESS
    
  Scenario: Get a Game using identifier.
    When taxonomy id is empty and Game Id is G1
    Then i should get Error message Taxonomy Id is blank and status is 400
    
  Scenario: Get a Game using identifier.
    When taxonomy id is absent and Game Id is G1
    Then i should get Error message Taxonomy Id is Requried and status is 400
    
  Scenario: Get a Game using identifier.
    When taxonomy id is NUMERACY and Game Id is jeetu
    Then i should get Error message Node not found and status is 404
  
  
    
  
	
	    
