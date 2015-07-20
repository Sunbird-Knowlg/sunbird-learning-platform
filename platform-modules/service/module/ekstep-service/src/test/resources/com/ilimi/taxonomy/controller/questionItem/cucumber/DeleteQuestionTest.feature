Feature: Test all scenarios of delete question.

  Scenario: Delete a question using identifier.
  	When Delete question when Taxonomy id is numeracy and question id is Q2
  	Then return status of delete question is successful and response code is 200
  
  Scenario: Create a question with taxonomy is empty.
  	When Delete question when Taxonomy id is empty and question id is Q2
  	Then return status of delete question is failed and response code is 400
    And get error message of delete question is Taxonomy Id is blank
 
  Scenario: Create a question with taxonomy is absent.
  	When Delete question when Taxonomy id is absent and question id is Q2
  	Then return status of delete question is failed and response code is 400
    And get error message of delete question is Required String parameter 'taxonomyId' is not present
    
    
  Scenario: Delete a question using wrong identifier.
  	When Delete question when Taxonomy id is numeracy and question id is ilimi
  	Then return status of delete question is failed and response code is 404
    Then get error message of delete question is Node not found: ilimi

    
    
    
    
    
  
