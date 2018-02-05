# Distributed Hash Table (DHT)

Implement a key-value store service:

- Service exposes a RESTful API that supports the following actions:

    + get: find key in the storage and return JSON-encoded key-value pair, ex: {"key": "my-key", "value": "my-val"}

    + put: write given (JSON-encoded) key-value pair to storage

    + delete: find key in storage and delete its key-value pair

    + size: return number of key-value pairs in the storage

    * keys are strings of 1-64 characters restricted to character set: a-zA-Z0-9_-

    * values are strings with maximum length of 1KB (1024 bytes), all characters are allowed (binary data)
	
	