Feature: Test all scenarios of create Questionnaire.

  Scenario: Create a Questionnaire.
    When Creating a Questionnaire Taxonomy id is numeracy with proper Questionnaire data
    Then return status of create Questionnaire is successful and response code is 200

  Scenario: Create a Questionnaire with taxonomy is empty.
    When Creating a Questionnaire Taxonomy id is empty with proper Questionnaire data
    Then return status of create Questionnaire is failed and response code is 400
    And return error message by create Questionnaire API is Taxonomy Id is blank

  Scenario: Create a Questionnaire with taxonomy is absent.
    When Creating a Questionnaire Taxonomy id is absent with proper Questionnaire data
    Then return status of create Questionnaire is failed and response code is 400
    And return error message by create Questionnaire API is Required String parameter 'taxonomyId' is not present
  
  Scenario: Create a Questionnaire with insufficient assessment items.
    When Creating a Questionnaire Taxonomy id is numeracy with insufficient assessment items
    Then return status of create Questionnaire is failed and response code is 400
    And return error message by create Questionnaire API is questionnaire has insufficient assessment items.
    
    When Creating a Questionnaire Taxonomy id is numeracy with wrong member id
    Then return status of create Questionnaire is failed and response code is 400
    And return error message by create Questionnaire API is Member Ids are invalid

  Scenario: Create a Questionnaire when Content data is not in correct format or missing.
    When Creating a Questionnaire Taxonomy id is numeracy with questionnaire as blank
    Then return status of create Questionnaire is failed and response code is 400
    And return error message by create Questionnaire API is Questionnaire Object is blank
    
    When Creating a Questionnaire Taxonomy id is numeracy with empty object type
    Then return status of create Questionnaire is failed and response code is 400
    And return error message by create Questionnaire API is Object type not set for node: null

    When Creating a Questionnaire Taxonomy id is numeracy with wrong definition node
    Then return status of create Questionnaire is failed and response code is 400
    And return error message by create Questionnaire API is Definition node not found for Object Type: ilimi

    When Creating a Questionnaire Taxonomy id is numeracy with require metadata
    Then return status of create Questionnaire is failed and response code is 400
    And return error message by create Questionnaire API is Required Metadata code not set

    When Creating a Questionnaire Taxonomy id is numeracy with unsupported relation
    Then return status of create Questionnaire is failed and response code is 400
    And return error message by create Questionnaire API is Relation ilimi is not supported

#    When Creating a Questionnaire Taxonomy id is numeracy with invalid data type for select
#    Then return status of create Questionnaire is failed and response code is 400
#    And return error message by create Questionnaire API is Metadata status should be one of: [assessment, worksheet]
