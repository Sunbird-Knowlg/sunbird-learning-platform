#Feature: Test all scenarios of create Questionnaire.

#  Scenario: Create a Questionnaire.
#    When Create Questionnaire Taxonomy is numeracy with proper Questionnaire data
#    Then return status of create Questionnaire is successful and response code is 200

#  Scenario: Create a Questionnaire with taxonomy is empty.
#    When Create Questionnaire Taxonomy is empty with proper Questionnaire data
#    Then return status of create Questionnaire is failed and response code is 400
#    And get error message of create Questionnaire is Taxonomy Id is blank

#  Scenario: Create a Questionnaire with taxonomy is absent.
#    When Create Questionnaire Taxonomy is absent with proper Questionnaire data
#    Then return status of create Questionnaire is failed and response code is 400
#    And get error message of create Questionnaire is Required String parameter 'taxonomyId' is not present

#  Scenario: Create a Questionnaire when Content data is not in correct format or missing.
#    When Create Questionnaire Taxonomy is numeracy with Questionnaire as blank
#    Then return status of create Questionnaire is failed and response code is 400
#    And get error message of create Questionnaire is Questionnaire Object is blank
#    When Create Questionnaire Taxonomy is numeracy with no Questionnaire name
#    Then return status of create Questionnaire is failed and response code is 400
#    And get error message of create Questionnaire is Required Metadata name not set
#    When Create Questionnaire Taxonomy is numeracy with Questionnaire metadata invalid list value
#    Then return status of create Questionnaire is failed and response code is 400
#    And get error message of create Questionnaire is Metadata status should be one of: [embedded, mcq, mmcq, sort_list, ftb, mtf, speech_Questionnaire", canvas_Questionnaire]
#    When Create Questionnaire Taxonomy is numeracy with unsupported relation
#    Then return status of create Questionnaire is failed and response code is 400
#    And get error message of create Questionnaire is Relation wrongRelation is not supported
#    When Create Questionnaire Taxonomy is numeracy with no required relations
#    Then return status of create Questionnaire is failed and response code is 400
#    And get error message of create Questionnaire is Required incoming relations are missing
#    When Create Questionnaire Taxonomy is numeracy with invalid relation node
#    Then return status of create Questionnaire is failed and response code is 400
#    And get error message of create Questionnaire is Node not found
