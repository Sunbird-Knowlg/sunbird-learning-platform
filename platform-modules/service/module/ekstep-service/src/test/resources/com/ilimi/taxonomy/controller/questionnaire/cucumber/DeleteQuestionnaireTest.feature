Feature: Test all scenarios of delete questionnaire.

  Scenario: Delete a questionnaire using identifier.
  	When Deleting a questionnaire Taxonomy id is numeracy and questionnaire id is numeracy_461
  	Then return status of delete questionnaire is successful and response code is 200
  
  Scenario: Delete a questionnaire with taxonomy is empty.
  	When Deleting a questionnaire Taxonomy id is empty and questionnaire id is numeracy_461
  	Then return status of delete questionnaire is failed and response code is 400
    And return error message by delete questionnaire API is Taxonomy Id is blank
 
  Scenario: Delete a questionnaire with taxonomy is absent.
  	When Deleting a questionnaire Taxonomy id is absent and questionnaire id is numeracy_461
  	Then return status of delete questionnaire is failed and response code is 400
    And return error message by delete questionnaire API is Required String parameter 'taxonomyId' is not present
 
  Scenario: Delete a questionnaire using wrong identifier.
  	When Deleting a questionnaire Taxonomy id is numeracy and questionnaire id is ilimi
   	Then return status of delete questionnaire is failed and response code is 404
    Then return error message by delete questionnaire API is Node not found: ilimi
