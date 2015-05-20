Feature: Test all scenarios of update Game.

  Scenario: Update a Game.
  	When i give input taxonomy ID is NUMERACY and concept ID is G1
    Then Update the Game and get the status SUCCESS
    
  Scenario: Update a Game when taxonomy id is empty.  	
  	When i give input taxonomy ID is empty and concept ID is G1
    Then i should get Error Message taxonomy Id is blank and status is 400
    
   Scenario: Update a concept when taxonomy id is not present.
  	When i give input taxonomy ID is absent and concept ID is G1 
    Then i should get Error Message taxonomy Id is Required and status is 400
    
  Scenario: Update a concept when concept object is blank.
  	When i give input taxonomy ID is NUMERACY and concept ID is G1
    Then i should get error message is Learning Object is blank
    
  Scenario: Update a Game when object type is empty.
  	When i give input taxonomy ID is NUMERACY and concept ID is G1
    Then i should get error message object type not set for node G1
    
  Scenario: Update a Game when metadata is missing.
  	When i give input taxonomy ID is NUMERACY and concept ID is G1
    Then i should get error message node metadata validation failed
    
  