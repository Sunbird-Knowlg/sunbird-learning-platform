Feature: Test all scenarios of update concept.

  Scenario: Update a concept.
  	When i give Taxonomy ID NUMERACY and concept is Num:C1
    Then update a Concept and get the status Successful
    
  Scenario: Update a concept when taxonomy id is empty.  	
  	When i give Taxonomy ID empty and concept is Num:C1
    Then i should get Error Message Taxonomy Id is blank and status is 400
    
   Scenario: Update a concept when taxonomy id is not present.
  	When i give Taxonomy ID absent and concept is Num:C1 
    Then i should get Error Message Taxonomy Id is Required and status is 400
    
  Scenario: Update a concept when concept object is blank.
  	When i give Taxonomy ID NUMERACY and concept is Num:C1
    Then i should get errMsg is Validation Error
    
  Scenario: Update a concept when object type is blank.
  	When i give Taxonomy ID NUMERACY and concept is Num:C1
    Then i should get errMsg is Concept Object is blank
    
  Scenario: Update a concept when metadata is missing.
  	When i give Taxonomy ID NUMERACY and concept is Num:C1
    Then i should get errMsg is metadata name is not set
    
  Scenario: Update a concept where relation is not supported.
   	When i give Taxonomy ID NUMERACY and concept is Num:C1
    Then i should get errMsg is Relation is not supported
    

    