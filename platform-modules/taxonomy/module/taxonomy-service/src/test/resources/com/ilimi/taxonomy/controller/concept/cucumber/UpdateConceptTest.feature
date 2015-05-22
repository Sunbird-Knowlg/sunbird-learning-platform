Feature: Test all scenarios of update concept.

  Scenario: Update a concept.
  	When i give Taxonomy ID numeracy and concept is Num:C1
    Then update a Concept and get the status successful
    
  Scenario: Update a concept when taxonomy id is empty.  	
  	When i give Taxonomy ID empty and concept is Num:C1
    Then i should get Error Message Taxonomy Id is blank and status is 400
    
   Scenario: Update a concept when taxonomy id is not present.
  	When i give Taxonomy ID absent and concept is Num:C1 
    Then i should get Error Message Taxonomy Id is Required and status is 400
    
  Scenario: Update a concept when concept object is blank.
  	When i give Taxonomy ID numeracy and concept is Num:C1
    Then i should get errMsg is concept object is blank
    
  Scenario: Update a concept using wron identifier.
  	When i give Taxonomy ID numeracy and concept is Num:C1
    Then i should get errMsg is node Not found
    
  Scenario: Update a concept when object type is blank.
  	When i give Taxonomy ID numeracy and concept is Num:C1
    Then i should get errMsg is concept object type is blank
    
  Scenario: Update a concept when metadata is missing.
  	When i give Taxonomy ID numeracy and concept is Num:C1
    Then i should get errMsg is metadata code is not set
    
  Scenario: Update a concept where node is invalid.
   	When i give Taxonomy ID numeracy and concept is Num:C1
    Then i should get errMsg is invalid node
    

    