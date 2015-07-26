Feature: Test all scenarios of get question.

  Scenario: Get a question using identifier.
    When Getting a question Taxonomy id is numeracy and question id is Q1
    Then return status of get question is successful and response code is 200    
    
   Scenario: Get a question with taxonomy is empty.
  	When Getting a question Taxonomy id is empty and question id is Q1
  	Then return status of get question is failed and response code is 400
    And return error message by get question API is Taxonomy Id is blank
 
  Scenario: Get a question with taxonomy is absent.
  	When Getting a question Taxonomy id is absent and question id is Q1
  	Then return status of get question is failed and response code is 400
    And return error message by get question API is Required String parameter 'taxonomyId' is not present
    
  Scenario: Get a question using wrong identifier.
  	When Getting a question Taxonomy id is numeracy and question id is ilimi
  	Then return status of get question is failed and response code is 404
    Then return error message by get question API is Node not found: ilimi
    

	
	
	    
