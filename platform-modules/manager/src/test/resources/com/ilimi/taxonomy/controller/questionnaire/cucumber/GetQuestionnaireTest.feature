Feature: Test all scenarios of get questionnaire.

  Scenario: Get a questionnaire using identifier.
    When Getting a questionnaire Taxonomy id is numeracy and questionnaire id is numeracy_464
    Then return status of get questionnaire is successful and response code is 200    
    
   Scenario: Get a questionnaire with taxonomy is empty.
  	When Getting a questionnaire Taxonomy id is empty and questionnaire id is numeracy_464
  	Then return status of get questionnaire is failed and response code is 400
    And return error message by get questionnaire API is Taxonomy Id is blank
 
  Scenario: Get a questionnaire with taxonomy is absent.
  	When Getting a questionnaire Taxonomy id is absent and questionnaire id is numeracy_464
  	Then return status of get questionnaire is failed and response code is 400
    And return error message by get questionnaire API is Required String parameter 'taxonomyId' is not present
    
  Scenario: Get a questionnaire using wrong identifier.
  	When Getting a questionnaire Taxonomy id is numeracy and questionnaire id is ilimi
  	Then return status of get questionnaire is failed and response code is 404
    Then return error message by get questionnaire API is Node not found: ilimi
