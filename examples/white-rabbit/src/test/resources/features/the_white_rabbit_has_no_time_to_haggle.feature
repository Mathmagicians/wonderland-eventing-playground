Feature: The White Rabbit has no time to haggle
  The White Rabbit is late for everything. It reads the stream of offers, and the first offer for a thing it
  wants is taken as it is: an order for that thing at that price, no haggling, and on it runs.

  Background:
    Given I can connect to the eventing system

  Scenario Outline: The White Rabbit orders at the first offer
    Given the White Rabbit wants a <Thing>
    And Mad Hatter offers a <Thing> for <Price> coins
    And March Hare offers a <Thing> for <Cheaper> coins
    When the white rabbit reads the stream of offers
    Then the White Rabbit ordered a <Thing> from Mad Hatter at <Price> coins
    And nothing else was ordered

    Examples:
      | Thing        | Price | Cheaper |
      | Pocket Watch | 20    | 15      |
      | Waistcoat    | 8.5   | 7       |
