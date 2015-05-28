Feature: Test all scenarios of get Taxonomy.

  Scenario: Get Taxonomy using identifier.
    When The taxonomy Id is numeracy
    Then I should get all the numeracy data
    
  Scenario: Get Taxonomy using wrong identifier.
    When The taxonomy Id is ilimi
    Then I should get Error message node not found is blank and status is 404
    
