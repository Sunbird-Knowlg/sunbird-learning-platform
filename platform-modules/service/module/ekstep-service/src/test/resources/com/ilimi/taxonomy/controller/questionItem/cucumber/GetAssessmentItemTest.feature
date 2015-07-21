Feature: Test all scenarios of get question.

  Scenario: Get a question using identifier.
    When Get question when Taxonomy id is numeracy and question id is Q1
    Then return status of get question is successful and response code is 200    
    
   Scenario: Get a question with taxonomy is empty.
  	When Get question when Taxonomy id is empty and question id is Q1
  	Then return status of get question is failed and response code is 400
    And get error message of get question is Taxonomy Id is blank
 
  Scenario: Get a question with taxonomy is absent.
  	When Get question when Taxonomy id is absent and question id is Q1
  	Then return status of get question is failed and response code is 400
    And get error message of get question is Required String parameter 'taxonomyId' is not present
    
  Scenario: Get a question using wrong identifier.
  	When Get question when Taxonomy id is numeracy and question id is ilimi
  	Then return status of get question is failed and response code is 404
    Then get error message of get question is Node not found: ilimi
    

	
	
	    
