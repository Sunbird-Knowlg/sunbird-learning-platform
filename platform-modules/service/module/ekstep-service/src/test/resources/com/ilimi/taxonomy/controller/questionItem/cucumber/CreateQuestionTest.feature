Feature: Test all scenarios of create Question.

  Scenario: Create a Question.
    When Create Question Taxonomy is numeracy with proper question data for mcq
    Then return status of create Question is successful and response code is 200
    
    When Create Question Taxonomy is numeracy with proper question data for mmcq
    Then return status of create Question is successful and response code is 200
    
    When Create Question Taxonomy is numeracy with proper question data for ftb
    Then return status of create Question is successful and response code is 200
    
    When Create Question Taxonomy is numeracy with proper question data for canvas_question
    Then return status of create Question is successful and response code is 200
    
    When Create Question Taxonomy is numeracy with proper question data for speech_question
    Then return status of create Question is successful and response code is 200
    
    When Create Question Taxonomy is numeracy with proper question data for mtf
    Then return status of create Question is successful and response code is 200
    
  Scenario: Create a Question with invalid options.
    When Create Question Taxonomy is numeracy with invalid question options for mcq
    Then return status of create Question is failed and response code is 400
    And get error message as invalid assessment item property: options. is_answer is missing.
    
    When Create Question Taxonomy is numeracy with multiple answer for mcq
    Then return status of create Question is failed and response code is 400
    And get error message as multiple answers found in a mcq assessment item.
    
    When Create Question Taxonomy is numeracy with no options for mcq
    Then return status of create Question is failed and response code is 400
    And get error message as no option found with answer.
    
    When Create Question Taxonomy is numeracy with no multiple answer for mmcq
    Then return status of create Question is failed and response code is 400
    And get error message as there are no multiple answer options.
 

  Scenario: Create a Question with taxonomy is empty.
    When Create Question Taxonomy is empty with proper question data
    Then return status of create Question is failed and response code is 400
    And get error message as Taxonomy Id is blank

  Scenario: Create a Question with taxonomy is absent.
    When Create Question Taxonomy is absent with proper question data
    Then return status of create Question is failed and response code is 400
    And get error message as Required String parameter 'taxonomyId' is not present

  Scenario: Create a Question when Content data is not in correct format or missing.
    When Create Question Taxonomy is numeracy with question as blank
    Then return status of create Question is failed and response code is 400
    And get error message as assessmentitem Object is blank
    
    When Create Question Taxonomy is numeracy with empty object type
    Then return status of create Question is failed and response code is 400
    And get error message as Object type not set for node: tQ2
    
    When Create Question Taxonomy is numeracy with wrong definition node
    Then return status of create Question is failed and response code is 400
    And get error message as Definition node not found for Object Type: ilimi
    
    When Create Question Taxonomy is numeracy with require metadata
    Then return status of create Question is failed and response code is 400
    And get error message as Required Metadata code not set
    
    When Create Question Taxonomy is numeracy with invalid data type for select
    Then return status of create Question is failed and response code is 400
    And get error message as Metadata status should be one of: [Draft, Review, Live, Retired, Mock]
    
    When Create Question Taxonomy is numeracy with unsupported relation
    Then return status of create Question is failed and response code is 400
    And get error message as Relation ilimi is not supported
    
#    When Create Question Taxonomy is numeracy with invalid data type
#    Then return status of create Question is failed and response code is 400
#    And get error message as invalid data type
    
