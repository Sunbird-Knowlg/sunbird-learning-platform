Feature: Test all scenarios of update question.

  Scenario: Update a question.
    When Update question when Taxonomy id is numeracy and question id is tQ1 with proper question data and metadata changes
    Then return status of update question is successful and response code is 200
    
    When Update question when Taxonomy id is numeracy and question id is tQ1 with proper question data and relation changes
    Then return status of update question is successful and response code is 200 

  Scenario: Update a question with taxonomy is empty.
    When Update question when Taxonomy id is empty and question id is tQ1 with proper question data
    Then return status of update question is failed and response code is 400
    And get error message of update question is Taxonomy id is blank

  Scenario: Update a question with taxonomy is absent.
    When Update question when Taxonomy id is absent and question id is Q1 with proper question data
    Then return status of update question is failed and response code is 400
    And get error message of update question is Required String parameter 'taxonomyid' is not present
    
  Scenario: Update a question using wrong identifier.
    When Update question when Taxonomy id is numeracy and question id is ilimi with wrong question id
    Then return status of update question is failed and response code is 404
    And get error message of update question is Node not found

  Scenario: Create a question when Content data is not in correct format or missing.
    When Update question when Taxonomy id is numeracy and question id is tQ1 with question as blank
    Then return status of update question is failed and response code is 400
    And get error message of update question is question Object is blank    
    
    When Update question when Taxonomy id is numeracy and question id is tQ1 with require metadata
    Then return status of update question is failed and response code is 400
    And get error message of update question is Required Metadata code not set
    
    When Update question when Taxonomy id is numeracy and question id is tQ1 with invalid data type for select
    Then return status of update question is failed and response code is 400
    And get error message of update question is Metadata status should be one of: [Draft, Review, Live, Retired, Mock]
    
    When Update question when Taxonomy id is numeracy and question id is tQ1 with object type not set
    Then return status of update question is failed and response code is 400
    And get error message of update question is Object type not set for node: tempQ
    
    When Update question when Taxonomy id is numeracy and question id is tQ1 with wrong definition node
    Then return status of update question is failed and response code is 400
    And get error message of update question is Definition node not found for Object Type: ilimi

    

    