Feature: BDD test scenarios for Game Service

  Scenario: Get list of games without any filters
    When Subject is blank
    Then return status is successful and response code is 200

	When Subject is numeracy
    Then return status is successful and response code is 200
    
    When Subject is invalid
    Then return status is successful and response code is 200 and games list size is eq 0
    
  Scenario: Get list of games with filters
    When Status is Mock
    Then return status is successful and response code is 200 and games list size is gt 0
    
    When Fields parameter is invalid
    Then return status is failed and error code is ERR_GAME_INVALID_PARAM and message is Request Parameter 'fields' should be a list
    
    When Fields parameter is name
    Then return status is successful and games list has only name
    
    When developer is Play Store and Status is Mock
    Then return status is successful and developer value is Play Store
    
    
    