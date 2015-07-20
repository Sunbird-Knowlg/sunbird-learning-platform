Feature: Test all scenarios of get questionnaire.

  Scenario: Get a questionnaire using identifier.
    When Get questionnaire when Taxonomy id is numeracy and questionnaire id is numeracy_411
    Then return status of get questionnaire is successful and response code is 200    
    
   Scenario: Get a questionnaire with taxonomy is empty.
  	When Get questionnaire when Taxonomy id is empty and questionnaire id is numeracy_411
  	Then return status of get questionnaire is failed and response code is 400
    And get error message of get questionnaire is Taxonomy Id is blank
 
  Scenario: Get a questionnaire with taxonomy is absent.
  	When Get questionnaire when Taxonomy id is absent and questionnaire id is numeracy_411
  	Then return status of get questionnaire is failed and response code is 400
    And get error message of get questionnaire is Required String parameter 'taxonomyId' is not present
    
  Scenario: Get a questionnaire using wrong identifier.
  	When Get questionnaire when Taxonomy id is numeracy and questionnaire id is ilimi
  	Then return status of get questionnaire is failed and response code is 404
    Then get error message of get questionnaire is Node not found: ilimi
