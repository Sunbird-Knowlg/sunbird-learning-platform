Feature: Test all scenarios of delete questionnaire.

  Scenario: Delete a questionnaire using identifier.
  	When Delete questionnaire when Taxonomy id is numeracy and questionnaire id is numeracy_461
  	Then return status of delete questionnaire is successful and response code is 200
  
  Scenario: Create a questionnaire with taxonomy is empty.
  	When Delete questionnaire when Taxonomy id is empty and questionnaire id is numeracy_461
  	Then return status of delete questionnaire is failed and response code is 400
    And get error message of delete questionnaire is Taxonomy Id is blank
 
  Scenario: Create a questionnaire with taxonomy is absent.
  	When Delete questionnaire when Taxonomy id is absent and questionnaire id is numeracy_461
  	Then return status of delete questionnaire is failed and response code is 400
    And get error message of delete questionnaire is Required String parameter 'taxonomyId' is not present
 
  Scenario: Delete a questionnaire using wrong identifier.
  	When Delete questionnaire when Taxonomy id is numeracy and questionnaire id is ilimi
   	Then return status of delete questionnaire is failed and response code is 404
    Then get error message of delete questionnaire is Node not found: ilimi
