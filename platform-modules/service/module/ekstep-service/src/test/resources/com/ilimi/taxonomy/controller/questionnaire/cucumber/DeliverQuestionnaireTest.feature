Feature: Test all scenarios of Deliver questionnaire.

  Scenario: Deliver a questionnaire using identifier.
    When Deliver questionnaire when Taxonomy id is numeracy and questionnaire id is numeracy_411
    Then return status of Deliver questionnaire is successful and response code is 200    
    
   Scenario: Deliver a questionnaire with taxonomy is empty.
  	When Deliver questionnaire when Taxonomy id is empty and questionnaire id is numeracy_411
  	Then return status of Deliver questionnaire is failed and response code is 400
    And get error message of Deliver questionnaire is Taxonomy Id is blank
 
  Scenario: Deliver a questionnaire with taxonomy is absent.
  	When Deliver questionnaire when Taxonomy id is absent and questionnaire id is numeracy_411
  	Then return status of Deliver questionnaire is failed and response code is 400
    And get error message of Deliver questionnaire is Required String parameter 'taxonomyId' is not present
    
  Scenario: Deliver a questionnaire using wrong identifier.
  	When Deliver questionnaire when Taxonomy id is numeracy and questionnaire id is ilimi
  	Then return status of Deliver questionnaire is failed and response code is 404
    Then get error message of Deliver questionnaire is Node not found: ilimi
