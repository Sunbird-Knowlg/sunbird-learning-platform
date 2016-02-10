Feature: Test all scenarios of get question.

  Scenario: Get a question using identifier.
    When Getting a question Taxonomy id is numeracy and question id is Q1
    Then return status of get question is successful and response code is 200    
    
  Scenario: Get a question using wrong identifier.
  	When Getting a question Taxonomy id is numeracy and question id is ilimi
  	Then return status of get question is failed and response code is 404
    Then return error message by get question API is Node not found: ilimi
    

	
	
	    
