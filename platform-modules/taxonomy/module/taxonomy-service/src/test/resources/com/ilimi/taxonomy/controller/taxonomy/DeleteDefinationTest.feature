Feature: Test all scenarios of delete defination.

  Scenario: Delete defination using identifier.
  	When Taxonomy ID is NUMERACY and objectType ID  is Game
    Then Delete the Game defination and get status SUCCESS
    
  Scenario: Delete defination using wrong objectType.
  	When Taxonomy ID is wrongNum and objectType ID  is Game
    Then Unable to delete the defination and get status 404