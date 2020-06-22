Feature: Context Root of this API
  In order to use the Testing API, it must be available

  Scenario: ContextRoot
    Given the testing application is alive
    When I navigate to https://trade.trevorism.com
    Then the API returns a link to the help page

  Scenario: Ping
    Given the testing application is alive
    When I navigate to /ping on https://trade.trevorism.com
    Then pong is returned, to indicate the service is alive
