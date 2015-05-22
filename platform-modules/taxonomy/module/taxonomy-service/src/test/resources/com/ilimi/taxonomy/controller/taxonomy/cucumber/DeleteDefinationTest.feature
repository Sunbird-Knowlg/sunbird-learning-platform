Feature: Test all scenarios of delete defination.

  Scenario: Delete defination using identifier.
  	When Taxonomy id is numeracy and objectType ID is Game
    Then Delete the Game defination and get status succussfull
    
  Scenario: Delete defination using wrong taxonomy.
  	When Taxonomy id is wrongId and objectType ID is Game
    Then Unable to delete the defination and get status 404