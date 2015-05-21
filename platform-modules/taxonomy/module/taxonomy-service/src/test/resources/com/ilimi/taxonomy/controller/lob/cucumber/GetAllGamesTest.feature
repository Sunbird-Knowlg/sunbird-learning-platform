Feature: Test all scenarios of get all concept.

  Scenario: Get all concept using identifier .
  	When i give taxonomy id is NUMERACY
    Then i should get all games with status SUCCESS
	            
  Scenario: Get a game using identifier with empty taxonomy Id.
  	When i give taxonomy id is empty
    Then i should get Message Taxonomy Id is blank and status is 400
    
  Scenario: Get a game using identifier without taxonomy Id.
  	When i give taxonomy id is absent 
    Then i should get Message Taxonomy Id is Required and status is 400
    
  
