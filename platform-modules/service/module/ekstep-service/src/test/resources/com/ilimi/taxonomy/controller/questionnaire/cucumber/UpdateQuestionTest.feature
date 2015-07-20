#Feature: Test all scenarios of update questionnaire.
#
#  Scenario: Update a questionnaire.
#    When Update questionnaire when Taxonomy id is numeracy and questionnaire id is Q1 with proper questionnaire data
#    Then return status of update questionnaire is successful and response code is 200
#
#  Scenario: Update a questionnaire with taxonomy is empty.
#    When Update questionnaire when Taxonomy id is empty and questionnaire id is Q1 with proper questionnaire data
#    Then return status of update questionnaire is failed and response code is 400
#    And get error message of update questionnaire is Taxonomy id is blank
#
#  Scenario: Update a questionnaire with taxonomy is absent.
#    When Update questionnaire when Taxonomy id is absent and questionnaire id is Q1 with proper questionnaire data
#    Then return status of update questionnaire is failed and response code is 400
#    And get error message of update questionnaire is Required String parameter 'taxonomyid' is not present
#    
#    
#  Scenario: Create a questionnaire when Content data is not in correct format or missing.
#    When Update questionnaire when Taxonomy id is numeracy and questionnaire id is Q1 with questionnaire is blank
#    Then return status of update questionnaire is failed and response code is 400
#    And get error message of update questionnaire is learning Object is blank    
#
#    When Update questionnaire when Taxonomy id is numeracy and questionnaire id is Q1 with object type not set
#    Then return status of update questionnaire is failed and response code is 400
#    And get error message of update questionnaire is Object type not set for node: Q1
#    
#
#
#
#
#
#
#    
#  