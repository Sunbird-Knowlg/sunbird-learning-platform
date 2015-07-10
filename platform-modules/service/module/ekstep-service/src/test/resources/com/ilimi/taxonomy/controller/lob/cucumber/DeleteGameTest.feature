Feature: Test all scenarios of delete Game.

  Scenario: Delete a Game using identifier.
  	When I Give input taxonomy ID numeracy and Game ID G1
    Then Delete the Game Id G1
  
  Scenario: Delete a Game using identifier.
  	When I Give input taxonomy ID empty and Game ID G1
    Then I should get Error Taxonomy Id is blank and status is 400
	
  Scenario: Delete a Game using identifier.
  	When I Give input taxonomy ID absent and Game ID G1
    Then I should get Error Taxonomy Id is Required and status is 400            

  Scenario: Delete a Game using identifier.
  	When I Give input taxonomy ID numeracy and Game ID jeetu
    Then I should get Error Node not found and status is 404