Feature: Test all scenarios of create Game.

  Scenario: Create a Game.
  	When i give taxonomy ID NUMERACY
    Then Create a Game and get the status Successful
    
  Scenario: Create a Game when taxonomy Id is empty.
  	When i give taxonomy ID empty
    Then i will get Error Message Taxonomy Id is blank and status is 400
    
  Scenario: Create a Game when taxonomy Id is absent.
  	When i give taxonomy ID absent 
    Then i will get Error Message Taxonomy Id is Required and status is 400
    
  Scenario: Create a Game when game object is blank.
  	When i give taxonomy ID NUMERACY
    Then i will get errMsg is blank learning object
    
  Scenario: Create a Game when object type is empty.
  	When i give taxonomy ID NUMERACY
    Then i will get errMsg is Game Object is empty
    
  Scenario: Create a Game with missing metadata.
  	When i give taxonomy ID NUMERACY
    Then i will get errMsg is metadata name is not set
    
  Scenario: Create a Game with invalid data.
  	When i give taxonomy ID NUMERACY
    Then i will get errMsg is validation error and status 400
  
 Scenario: Unspported Relation for creating a Game .
  	When I give Taxonomy ID NUMERACY
    Then i will get errMsg is Relation is not supported
    

    
 
  
  
  