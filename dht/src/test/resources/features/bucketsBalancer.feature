Feature: The current node is balanced after the Buckets Balancer Scheduler is running

Scenario: The Current Node copy the Buckets that are not replicated
    Given that 3 more nodes are started 
    And and the Buckets "1, 3, 7, 30" are only on one Node each
    And the Current Node has no Bucket
    When the Bucket Balancer is running
    Then the node "1, 3, 7, 30" are copied on the Current Node  
    
Scenario: The Current Node transfer Buckets from the node that are busy
    Given that 3 more nodes are started
    And all 3 Nodes are storing all the Buckets
    And the Current Node has no Bucket
    And the replication factor is 2
    And the total number of Buckets is 64 
    When the Bucket Balancer is running
    Then the Current Node is transferring 32 Buckets from the rest of the Nodes
    