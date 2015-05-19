Feature: Test all scenarios of delete Taxonomy.

  Scenario: Delete Taxonomy.
  	When I Give input Taxonomy ID NUMERACY
    Then Delete the NUMERACY graph and get the status SUCCESS
    
   Scenario: Delete taxonomy using wrong identifier.
  	When I Give input Taxonomy ID NUMERACY
    Then delete the NUMERACY graph and get the status 404