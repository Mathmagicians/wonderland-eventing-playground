@manual
Feature: Wonderland's lines: what a feature reuses from the platform
  The platform ships the shared steps of the BDD suite, and a client's feature mixes them with its own. A line
  that opens with Wonderland is the platform's and names the element of the hexagon it drives; every other line
  is the client's. The summary counts them, so a feature says how much of it is Wonderland's.

  Rule: A platform step opens with Wonderland and names the element of the hexagon it drives, in the present tense

    Scenario Outline: A platform step names its element
      Given the step the platform ships for <Element>
      When I read its pattern
      Then it opens with Wonderland, as <Pattern>

      Examples:
        | Element              | Pattern                                                                                                            |
        | the rim              | Wonderland is reachable                                                                                            |
        | what the story hears | Wonderland hears on the stream of offers: {wonderlander} offers a {thing} for {bigdecimal} coins                   |
        | what the story hears | Wonderland hears on the stream of orders: {wonderlander} orders a {thing}                                          |
        | what the story hears | Wonderland hears on the stream of transactions: {wonderlander} paid {wonderlander} {bigdecimal} coins for a thing  |
        | the story            | Wonderland's story {story} reads the stream(s) of {payloads}                                                       |
        | the publisher        | Wonderland publishes {payloads} from region {word}                                                                 |
        | the publisher        | Wonderland publishes {payloads}                                                                                    |
        | the publisher        | Wonderland publishes {payloads} again                                                                              |
        | Confluent Cloud      | Wonderland has the {topic}                                                                                         |
        | Confluent Cloud      | Wonderland has the schema {word} for the {topic}                                                                   |
        | Confluent Cloud      | Wonderland has a schema for the {topic}                                                                            |
        | Confluent Cloud      | Wonderland's schema describes the message {word}                                                                   |
        | Confluent Cloud      | Wonderland's schema evolves with {word} compatibility                                                              |
        | Confluent Cloud      | Wonderland's table {word} shows each product of region {word} once, in its latest version, with its versions counted |

  Rule: A client's step never opens with Wonderland

    Scenario Outline: A client's own lines open with its domain
      Given the feature <Feature> rewritten to ADR-002
      When I read the lines the client defines
      Then none opens with Wonderland

      Examples:
        | Feature                                  |
        | At the tea party an order meets an offer |

  Rule: The opening and the glue agree

    Scenario Outline: Every line of a feature is glued where its opening says
      Given the feature <Feature> played by make bdd
      When I run make bdd-reuse
      Then it is green
      And every line that opens with Wonderland matched a step in the platform's bdd package
      And every other line matched a step outside it

      Examples:
        | Feature                                  |
        | At the tea party an order meets an offer |

    Scenario Outline: A line glued against its opening is named
      Given a report where a line that opens <Opening> matched a step <Glue>
      When I run make bdd-reuse on it
      Then it is red and names the line

      Examples:
        | Opening            | Glue                               |
        | with Wonderland    | outside the platform's bdd package |
        | without Wonderland | in the platform's bdd package      |

  Rule: The summary prints the reuse per feature, by element

    Scenario Outline: A feature's row
      Given the feature <Feature> played by make bdd
      When I run make bdd-reuse
      Then its row reads <Lines> lines, <Rim> rim, <Heard> heard, <Story> story, <Published> published, <Cloud> cloud, <Own> own, <Share> Wonderland's

      Examples:
        | Feature                                  | Lines | Rim | Heard | Story | Published | Cloud | Own | Share |
        | At the tea party an order meets an offer | 24    | 1   | 9     | 4     | 0         | 0     | 10  | 58%   |
