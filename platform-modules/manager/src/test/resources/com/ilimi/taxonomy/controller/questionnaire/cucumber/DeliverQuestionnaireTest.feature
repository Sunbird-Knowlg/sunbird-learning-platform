Feature: Test all scenarios of Deliver questionnaire.

  Scenario: Deliver a questionnaire using identifier.
    When Delivering a questionnaire Taxonomy id is numeracy and questionnaire id is numeracy_464
    Then return status of deliver questionnaire is successful and response code is 200    
    
   Scenario: Deliver a questionnaire with taxonomy is empty.
  	When Delivering a questionnaire Taxonomy id is empty and questionnaire id is numeracy_464
  	Then return status of deliver questionnaire is failed and response code is 400
    And return error message by deliver questionnaire API is Taxonomy Id is blank
 
  Scenario: Deliver a questionnaire with taxonomy is absent.
  	When Delivering a questionnaire Taxonomy id is absent and questionnaire id is numeracy_464
  	Then return status of deliver questionnaire is failed and response code is 400
    And return error message by deliver questionnaire API is Required String parameter 'taxonomyId' is not present
    
  Scenario: Deliver a questionnaire using wrong identifier.
  	When Delivering a questionnaire Taxonomy id is numeracy and questionnaire id is ilimi
  	Then return status of deliver questionnaire is failed and response code is 404
    Then return error message by deliver questionnaire API is Node not found: ilimi
