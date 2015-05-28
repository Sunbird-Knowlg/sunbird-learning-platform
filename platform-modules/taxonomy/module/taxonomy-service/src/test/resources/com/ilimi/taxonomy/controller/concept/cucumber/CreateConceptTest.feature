Feature: Test all scenarios of create concept.

  Scenario: Create a concept.
  	When I give Taxonomy ID numeracy
    Then Create a Concept and get the status successful
    
  Scenario: Create a concept when taxonomy id is empty.
  	When I give Taxonomy ID empty
    Then I should get Error Message Taxonomy Id is blank and status is 400
    
   Scenario: Create a concept when taxonomy id is absent.
  	When I give Taxonomy ID absent 
    Then I should get Error Message Taxonomy Id is Required and status is 400
    
  Scenario: Create a concept when concept object is blank.
  	When I give Taxonomy ID numeracy
    Then I should get errMsg is Validation Error
    
  Scenario: Create a concept when object type is empty.
  	When I give Taxonomy ID numeracy
    Then I should get errMsg is Concept Object is blank
   
  Scenario: Create a concept when defination node found.
  	When I give Taxonomy ID numeracy
    Then I should get errMsg is node not found 
    
  Scenario: Create a concept when metadata is missing.
  	When I give Taxonomy ID numeracy
    Then I should get errMsg is metadata name is not set
    
  Scenario: Create a concept when datatype is invalid.
  	When I give Taxonomy ID numeracy
    Then I should get errMsg is datatype is invalid
    
  Scenario: Create a concept with unsupported relation.
  	When I give Taxonomy ID numeracy
    Then I should get errMsg is Relation is not supported
    
  Scenario: Create a concept when incoming and outgoing relation missing. 								
  	When I give Taxonomy ID numeracy
    Then Required incoming relations are missing
    