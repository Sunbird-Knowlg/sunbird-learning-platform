Feature: Test all scenarios of update questionnaire.

  Scenario: Update a questionnaire.
    When Update questionnaire when Taxonomy id is numeracy and questionnaire id is Q1 with proper questionnaire data
    Then return status of update questionnaire is successful and response code is 200

  Scenario: Update a questionnaire when taxonomy is wrong.
    When Update questionnaire when Taxonomy id is empty and questionnaire id is Q1 with proper questionnaire data
    Then return status of update questionnaire is failed and response code is 400
    And get error message of update questionnaire is Taxonomy id is blank

    When Update questionnaire when Taxonomy id is absent and questionnaire id is Q1 with proper questionnaire data
    Then return status of update questionnaire is failed and response code is 400
    And get error message of update questionnaire is Required String parameter 'taxonomyid' is not present
  
  Scenario: Update a questionnaire with wrong questionnaire id.
    When Update questionnaire when Taxonomy id is numeracy and questionnaire id is ilimi with wrong questionnaire Id
    Then return status of update questionnaire is failed and response code is 404
    And get error message of update questionnaire is node not found: ilimi
   
   Scenario: Update a Questionnaire with insufficient assessment items.
    When Update questionnaire when Taxonomy id is numeracy and questionnaire id is Q1 with insufficient assessment items
    Then return status of update questionnaire is failed and response code is 400
    And get error message of update questionnaire is questionnaire has insufficient assessment items.
    
    When Update questionnaire when Taxonomy id is numeracy and questionnaire id is Q1 with wrong member id
    Then return status of update questionnaire is failed and response code is 400
    And get error message of update questionnaire is Member with identifier: Q109 does not exist.
    
  Scenario: Create a questionnaire when Content data is not in correct format or missing.
    When Update questionnaire when Taxonomy id is numeracy and questionnaire id is Q1 with questionnaire is blank
    Then return status of update questionnaire is failed and response code is 400
    And get error message of update questionnaire is questionnaire Object is blank    

    When Update questionnaire when Taxonomy id is numeracy and questionnaire id is Q1 with empty object type
    Then return status of update questionnaire is failed and response code is 400
    And get error message of update questionnaire is Object type not set for node: null

    When Update questionnaire when Taxonomy id is numeracy and questionnaire id is Q1 with wrong definition node
    Then return status of update questionnaire is failed and response code is 400
    And get error message of update questionnaire is Definition node not found for Object Type: ilimi   