Feature: Test all scenarios of delete question.

  Scenario: Delete a question using identifier.
  	When Deleting a Question Taxonomy id is numeracy and question id is Q4
  	Then return status of delete question is successful and response code is 200
  
  Scenario: Delete a question with taxonomy is empty.
  	When Deleting a Question Taxonomy id is empty and question id is Q4
  	Then return status of delete question is failed and response code is 400
    And return error message by delete question API is Taxonomy Id is blank
 
  Scenario: Delete a question with taxonomy is absent.
  	When Deleting a Question Taxonomy id is absent and question id is Q4
  	Then return status of delete question is failed and response code is 400
    And return error message by delete question API is Required String parameter 'taxonomyId' is not present
    
    
  Scenario: Delete a question using wrong identifier.
  	When Deleting a Question Taxonomy id is numeracy and question id is ilimi
  	Then return status of delete question is failed and response code is 404
    Then return error message by delete question API is Node not found: ilimi

    
    
    
    
    
  
