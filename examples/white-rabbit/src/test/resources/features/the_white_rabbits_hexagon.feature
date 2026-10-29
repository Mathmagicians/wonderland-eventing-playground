@architecture
Feature: The White Rabbit's hexagon
  The White Rabbit, a story playing in Wonderland. This is the architecture definition; this feature verifies
  that the architecture is built as specified.

  Background:
    Given the architecture written by the build
    And the module "White Rabbit Story", @Module on the package stories.whiterabbit

  Scenario: NoTimeToHaggle is the White Rabbit's story
    Then its @PrimaryPort is NoTimeToHaggle, which implements Story
    And NoTimeToHaggle listensTo Offer
    And NoTimeToHaggle publishes Order through PublishMessage

  Scenario: WhiteRabbitStory wires the White Rabbit from its settings
    Then its @AutoConfiguration is WhiteRabbitStory, listed in META-INF/spring
    And WhiteRabbitStory has @EnableConfigurationProperties for WhiteRabbitProperties, the @ConfigurationProperties "white-rabbit"
    And its @Bean whiteRabbit builds NoTimeToHaggle from WhiteRabbitProperties

  Scenario: The White Rabbit stays inside its hexagon
    Then the module depends on application and domain only

  Scenario: Every arrow of the White Rabbit is tested
    Then every arrow of the module names a scenario of the_white_rabbit_has_no_time_to_haggle.feature

  Scenario: Nothing else is annotated
    Then the module carries no annotation this feature does not state
