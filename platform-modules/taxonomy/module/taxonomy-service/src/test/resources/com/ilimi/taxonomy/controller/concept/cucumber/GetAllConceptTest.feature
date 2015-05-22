Feature: Test all scenarios of get all concept.

  Scenario: Get all concept using identifier .
  	When Taxonomy Id is numeracy
    Then I should get all numeracy Concepts and status is successful
	            
  Scenario: Get a concept using identifier with empty taxonomy Id.
  	When Taxonomy Id is blank
    Then I should get ErrorMessage Taxonomy Id is blank and status is 400
    
  Scenario: Get a concept using identifier without taxonomy Id.
  	When Taxonomy Id is absent 
    Then I should get ErrorMessage Taxonomy Id is Required and status is 400
    
  

	
	    
