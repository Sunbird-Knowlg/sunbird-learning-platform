	Feature: Test all scenarios of create Question.

  Scenario: Create a Question.
  	When create question data for Assessment-items
  	
    When Creating a Question Taxonomy id is numeracy with proper question data for mcq
    Then return status of create Question is successful and response code is 200
    
    When Creating a Question Taxonomy id is numeracy with proper question data for mmcq
    Then return status of create Question is successful and response code is 200
    
    When Creating a Question Taxonomy id is numeracy with proper question data for ftb
    Then return status of create Question is successful and response code is 200
    
    When Creating a Question Taxonomy id is numeracy with proper question data for canvas_question
    Then return status of create Question is successful and response code is 200
    
    When Creating a Question Taxonomy id is numeracy with proper question data for speech_question
    Then return status of create Question is successful and response code is 200
    
    When Creating a Question Taxonomy id is numeracy with proper question data for mtf
    Then return status of create Question is successful and response code is 200
    
  Scenario: Create a Question with invalid options.
    When Creating a Question Taxonomy id is numeracy with invalid question options for mcq
    Then return status of create Question is failed and response code is 400
    And return error message as no option found with answer.
    
    When Creating a Question Taxonomy id is numeracy with invalid question options for ftb
    Then return status of create Question is failed and response code is 400
    And return error message as answer is missing.
    
    When Creating a Question Taxonomy id is numeracy with invalid question options for mtf
    Then return status of create Question is failed and response code is 400
    And return error message as invalid assessment item property: lhs_options. index is missing.    
    
    When Creating a Question Taxonomy id is numeracy with multiple answer for mcq
    Then return status of create Question is failed and response code is 400
    And return error message as no option found with answer.
    
    When Creating a Question Taxonomy id is numeracy with no options for mcq
    Then return status of create Question is failed and response code is 400
    And return error message as no option found with answer.
    
    When Creating a Question Taxonomy id is numeracy with no multiple answer for mmcq
    Then return status of create Question is failed and response code is 400
    And return error message as there are no multiple answer options.
    
    When Creating a Question Taxonomy id is numeracy with wrong answer for ftb
    Then return status of create Question is failed and response code is 400
    And return error message as Metadata answer should be a valid JSON.
    
   Scenario: Create a Question with invalid no of answers.
    
#    When Creating a Question Taxonomy id is numeracy with invalid no. of answer for mmcq
#    Then return status of create Question is failed and response code is 400
#    And return error message as invalid assessment item property: options. is_answer is missing.
    
    When Creating a Question Taxonomy id is numeracy with invalid no. of answer for ftb
    Then return status of create Question is failed and response code is 400
    And return error message as invalid assessment item property: options. is_answer is missing.
        
    When Creating a Question Taxonomy id is numeracy with invalid no. of answer for canvas
    Then return status of create Question is failed and response code is 400
    And return error message as invalid assessment item property: options. is_answer is missing.
    
    When Creating a Question Taxonomy id is numeracy with invalid no. of answer for speech
    Then return status of create Question is failed and response code is 400
    And return error message as invalid assessment item property: options. is_answer is missing. 

  Scenario: Create a Question when Content data is not in correct format or missing.
    When Creating a Question Taxonomy id is numeracy with question as blank
    Then return status of create Question is failed and response code is 400
    And return error message as assessmentitem Object is blank
    
    When Creating a Question Taxonomy id is numeracy with empty object type
    Then return status of create Question is failed and response code is 400
    And return error message as Object type not set for node: tQ2
    
    When Creating a Question Taxonomy id is numeracy with wrong definition node
    Then return status of create Question is failed and response code is 400
    And return error message as Definition node not found for Object Type: ilimi
    
    When Creating a Question Taxonomy id is numeracy with require metadata
    Then return status of create Question is failed and response code is 400
    And return error message as Required Metadata code not set
    
    When Creating a Question Taxonomy id is numeracy with invalid data type for select
    Then return status of create Question is failed and response code is 400
    And return error message as Metadata status should be one of: [Draft, Review, Live, Retired, Mock]
    
    When Creating a Question Taxonomy id is numeracy with unsupported relation
    Then return status of create Question is failed and response code is 400
    And return error message as relation ilimi is not supported
    
    When Creating a Question Taxonomy id is numeracy with invalid data type
    Then return status of create Question is failed and response code is 400
    And return error message as invalid data type
    
