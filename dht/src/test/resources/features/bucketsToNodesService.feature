Feature: 
    A Node should expose a REST API that allow the administration of the Buckets To Nodes mapping
    and a Node should properly initialize the Buckets To Nodes mapping when is starting

    Scenario: Node expose a REST API that return the Buckets To Nodes mapping    
        Given that a Node is started
        When I call the REST API GET "/bucketsToNodes" of the Node 
        Then I receive a JSON with the Buckets To Nodes mapping
        
    Scenario: Node expose a REST API that add mappings into the Buckets To Nodes mapping 
        Given that a Node is started
        When I call the REST API POST "/bucketsToNodes" of the Node with a JSON with new mappings 
        Then the new mappings are added to the Buckets To Node if they are not already there
        And the Buckets To Nodes mapping is returned as JSON
        
    Scenario: Node expose a REST API that remove mappings from the Buckets To Nodes mapping 
        Given that a Node is started
        And the Mapping To Nodes contains in bucket "1" Node with host "localhost" and port "8082"
        When I call the REST API DELETE "/bucketsToNodes/1/localhost/8082" 
        Then the mapping is removed from the Buckets To Nodes
        And the new Buckets To Nodes mapping is stored on disk for recovery    