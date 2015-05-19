Feature: Test all scenarios of create concept.

  Scenario: Create a concept.
  	When I give Taxonomy ID NUMERACY
    Then Create a Concept and get the status Successful
    
  Scenario: Create a concept.
  	When I give Taxonomy ID empty
    Then I should get Error Message Taxonomy Id is blank and status is 400
    
   Scenario: Create a concept.
  	When I give Taxonomy ID absent 
    Then I should get Error Message Taxonomy Id is Required and status is 400
    
  Scenario: Create a concept.
  	Given Concept object is blank
  	When I give Taxonomy ID NUMERACY
    Then I should get errMsg is Validation Error
    
  Scenario: Create a concept.
  	Given Object type is empty
  	When I give Taxonomy ID NUMERACY
    Then I should get errMsg is Concept Object is blank
    
  Scenario: Create a concept.
  	Given Missing metadata
  	When I give Taxonomy ID NUMERACY
    Then I should get errMsg is metadata name is not set
    
  Scenario: Create a concept.
  	Given Unspported Relation
  	When I give Taxonomy ID NUMERACY
    Then I should get errMsg is Relation is not supported
    
  Scenario: Create a concept.
  	Given incoming outgoing relation 
  	When I give Taxonomy ID NUMERACY
    Then Required incoming relations are missing
    