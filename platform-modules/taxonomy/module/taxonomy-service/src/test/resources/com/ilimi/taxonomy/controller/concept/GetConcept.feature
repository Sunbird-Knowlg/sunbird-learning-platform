Feature: Test all scenarios of get concept.

  Scenario: Get a concept using identifier.
    When I give Taxonomy NUMERACY and Concept ID Num:C1
    Then I should get the concept with name Geometry

