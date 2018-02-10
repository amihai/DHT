Feature: 
    Service exposes a RESTful API that supports the following actions:
        get: find key in the storage and return JSON-encoded key-value pair, ex: {"key": "my-key", "value": "my-val"}
        put: write given (JSON-encoded) key-value pair to storage
        delete: find key in storage and delete its key-value pair
        size: return number of key-value pairs in the storage
        keys are strings of 1-64 characters restricted to character set: a-zA-Z0-9_-
        values are strings with maximum length of 1KB (1024 bytes), all characters are allowed (binary data)
 

    Scenario: Node expose a rest API that allow us to store key-value pairs on disk
        Given that the bucket for key "my-key" is stored on current node
        When I call the PUT API "/keyValue" with the JSON {"key": "my-key", "value": "my-val"}
        Then I should see the value "my-val" stored on disk under the key "my-key"      
          
    Scenario: Node expose a rest API that allow us to get the value for a key from disk         
        Given that the bucket for key "my-key" is stored on current node
        And I already have stored the value "my-val" for the key "my-key" on the current node 
        When I call the GET API "/keyValue/my-key"
        Then I should receive the KeyValue pair {"key": "my-key", "value": "my-val"}
        
    Scenario: The GET API Return 404 status code if the key is not on disk        
        Given that the bucket for key "my-key" is stored on current node
        And I don't have the key "my-key" stored on current node
        When I call the GET API "/keyValue/my-key"
        Then I should receive an empty KeyValue
        
    Scenario: Node expose a rest API that allow us to remove a key from disk         
        Given that the bucket for key "my-key" is stored on current node
        And I already have stored the value "my-val" for the key "my-key" on the current node 
        When I call the GET DELETE "/keyValue/my-key"
        Then the key "my-key" is no longer stored on disk
        
    Scenario: Node expose a rest API to get the size of the storage   
        Given that 2 distinct pairs of key value are stored on disk   
        When I call the GET API "/keyValue/size" I should receive the value 2
        
        