Feature: Test all scenarios of get Taxonomy.

  Scenario: Get Taxonomy using identifier.
    When The taxonomy Id is NUMERACY
    Then I should get all the NUMERACY data
    
  Scenario: Get Taxonomy using wrong identifier.
    When The taxonomy Id is jeetu
    Then I should get Error message node not found is blank and status is 404
    
