Feature: 
    The Buckets are properly balanced when we add or remove Nodes
    and all the Buckets are reachable from all the Nodes
 
 @Ignore
    Scenario: When we add only one node we have all the Buckets stored on it
        Given no Node is started yet
        When I start the first Node
        Then I should have all the Buckets stored on this Node
 @Ignore       
    Scenario: A Node should be able to transfer Buckets from any Node on startup
        Given that I have started multiple Nodes   
        When a new Node is started
        Then I should be able to transfer Buckets from old Nodes to the new Node   
@Ignore
    Scenario: A Node should be able to transfer Buckets to any Node on graceful shutdown
        Given that I have started multiple Nodes   
        When a Node is graceful shutdown
        Then I should be able to transfer Buckets from dying Nodes to any Node      
 @Ignore    
    Scenario: When we add multiple nodes we have the buckets well balanced for redundancy and performance 
        Given the I already have few Nodes started
        When I start multiple Nodes
        Then I should have each bucket stored in at least two Nodes
        And each Node should store the same number of Buckets
 @Ignore       
    Scenario: When we shutdown Nodes the Buckets are re-balanced for redundancy and performance 
        Given that I have started multiple Nodes
        When I remove one by one the Nodes until only two remains 
        Then I should have all the Buckets stored on both Nodes
@Ignore
    Scenario: A Node should be able to reach a Bucket that is not stored on it 
        Given that I have started multiple Nodes
        When a Node need to reach a Bucket that is not stored on it
        Then a mapping between the Buckets and Nodes should be stored on each Node 
 @Ignore       
    Scenario: The Buckets and Nodes mapping from each Node should be up to date
        Given that I have started multiple Nodes
        And each Node contains a Buckets and Nodes mapping 
        When a Bucket is add or removed to/from a Node 
        Then the mapping between the Buckets and Nodes should be updated on all Nodes           
          
          
        
        