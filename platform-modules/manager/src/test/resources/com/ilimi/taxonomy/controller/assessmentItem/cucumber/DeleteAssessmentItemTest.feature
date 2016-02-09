Feature: Test all scenarios of delete question.

  Scenario: Delete a question using identifier.
  	When Deleting a Question Taxonomy id is numeracy and question id is tempQ
  	Then return status of delete question is successful and response code is 200
    
  Scenario: Delete a question using wrong identifier.
  	When Deleting a Question Taxonomy id is numeracy and question id is ilimi
  	Then return status of delete question is failed and response code is 404
    Then return error message by delete question API is Node not found: ilimi

    
    
    
    
    
  
