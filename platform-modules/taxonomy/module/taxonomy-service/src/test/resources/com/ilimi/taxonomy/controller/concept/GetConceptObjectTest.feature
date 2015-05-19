Feature: Test all scenarios of get concept object.

  Scenario: Get a concept object using identifier.
    When Concept ID is Num:C1 with relation type isParentOf and taxonomy Id is NUMERACY
    Then Get the relation object and status is successful
    
  Scenario: Get a concept object using identifier.
    When Concept ID is Num:C1 with relation type isParentOf and taxonomy Id is empty
    Then I Should get ErrMsg Taxonomy Id is blank and status is 400
    
  Scenario: Get a concept object using identifier.
    When Concept ID is Num:C1 with relation type isParentOf and taxonomy Id is absent
    Then I Should get ErrMsg Taxonomy Id is Required and status is 400
    
  
    