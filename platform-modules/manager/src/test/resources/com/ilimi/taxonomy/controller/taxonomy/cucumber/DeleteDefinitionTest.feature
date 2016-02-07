Feature: Test all scenarios of delete definition.

  Scenario: Delete definition using identifier.
  	When Taxonomy id is numeracy and objectType ID is Game
    Then Delete the Game definition and get status succussfull
    
  Scenario: Delete definition using wrong taxonomy.
  	When Taxonomy id is wrongId and objectType ID is Game
    Then Unable to delete the definition and get status 404