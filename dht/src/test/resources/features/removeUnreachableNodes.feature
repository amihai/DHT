Feature: Unreachable nodes are removed from the Buckets To Nodes Mapping after a configurable times of failure

Scenario: Remove a node from the Buckets To Nodes Mapping that is unreachable
    Given that the current node is started
    And the unreachable Node with host "localhost" and port "6666" is add to the Buckets To Nodes Mapping
    When the Buckets Balancing is running
    Then the Node with host "localhost" and port "6666" is removed from the Buckets To Nodes Mapping