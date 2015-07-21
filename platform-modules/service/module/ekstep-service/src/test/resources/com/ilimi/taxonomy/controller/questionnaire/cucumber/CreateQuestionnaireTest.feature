Feature: Test all scenarios of create Questionnaire.

  Scenario: Create a Questionnaire.
    When Create Questionnaire Taxonomy is numeracy with proper Questionnaire data
    Then return status of create Questionnaire is successful and response code is 200

  Scenario: Create a Questionnaire with taxonomy is empty.
    When Create Questionnaire Taxonomy is empty with proper Questionnaire data
    Then return status of create Questionnaire is failed and response code is 400
    And get error message of create Questionnaire is Taxonomy Id is blank

  Scenario: Create a Questionnaire with taxonomy is absent.
    When Create Questionnaire Taxonomy is absent with proper Questionnaire data
    Then return status of create Questionnaire is failed and response code is 400
    And get error message of create Questionnaire is Required String parameter 'taxonomyId' is not present
  
  Scenario: Create a Questionnaire with insufficient assessment items.
    When Create Questionnaire Taxonomy is numeracy with insufficient assessment items
    Then return status of create Questionnaire is failed and response code is 400
    And get error message of create Questionnaire is questionnaire has insufficient assessment items.
    
    When Create Questionnaire Taxonomy is numeracy with wrong member id
    Then return status of create Questionnaire is failed and response code is 400
    And get error message of create Questionnaire is Member Ids are invalid

  Scenario: Create a Questionnaire when Content data is not in correct format or missing.
    When Create Questionnaire Taxonomy is numeracy with questionnaire as blank
    Then return status of create Questionnaire is failed and response code is 400
    And get error message of create Questionnaire is Questionnaire Object is blank
    
    When Create Questionnaire Taxonomy is numeracy with empty object type
    Then return status of create Questionnaire is failed and response code is 400
    And get error message of create Questionnaire is Object type not set for node: null

    When Create Questionnaire Taxonomy is numeracy with wrong definition node
    Then return status of create Questionnaire is failed and response code is 400
    And get error message of create Questionnaire is Definition node not found for Object Type: ilimi

    When Create Questionnaire Taxonomy is numeracy with require metadata
    Then return status of create Questionnaire is failed and response code is 400
    And get error message of create Questionnaire is Required Metadata code not set

    When Create Questionnaire Taxonomy is numeracy with unsupported relation
    Then return status of create Questionnaire is failed and response code is 400
    And get error message of create Questionnaire is Relation ilimi is not supported

#    When Create Questionnaire Taxonomy is numeracy with invalid data type for select
#    Then return status of create Questionnaire is failed and response code is 400
#    And get error message of create Questionnaire is Metadata status should be one of: [assessment, worksheet]
