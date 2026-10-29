@architecture-discovery
Feature: The purse has a hexagonal architecture
  Alice's purse, a story playing in Wonderland. This is the architecture definition; this feature verifies that
  the architecture is built as specified.

  Background:
    Given the architecture written by the build
    And the module "Purse Story", @Module on the package stories.purse

  Scenario: KnowWhatIsLeft is the purse's story
    Then its @PrimaryPort is KnowWhatIsLeft, which implements Story
    And KnowWhatIsLeft subscribesTo Transaction
    And KnowWhatIsLeft publishes nothing

  Scenario: PurseStory wires the purse from its settings
    Then its @AutoConfiguration is PurseStory, listed in META-INF/spring
    And PurseStory has @EnableConfigurationProperties for PurseProperties, the @ConfigurationProperties "purse"
    And its @Bean purse builds KnowWhatIsLeft from PurseProperties

  Scenario: The purse stays inside its hexagon
    Then the module depends on application and domain only

  Scenario: Every arrow of the purse is tested
    Then every payload it subscribes to or publishes is driven by a scenario of its feature

  Scenario: Nothing else is annotated
    Then the module carries no Hexagonal Architecture stereotypes this feature does not state
    And the module carries no Spring wiring this feature does not state
