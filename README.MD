### Task 6. Cache implementation.

##### Author: Nikita Semeniuk.

#### This application implements LFU and LRU caches. A custom library and a third-party Jackson library, which is also used for parsing in XML, were used as a library for parsing JSON strings. A proxy repository is implemented to connect the repository and cache. To convert user input and maintain an interactive dialogue with the user, an abstraction in the form of a message assistant is implemented above the controller layer. The assistant plays the role of a message converter and passes requests to the appropriate controller methods for their subsequent processing. The configuration data for the application is defined in the properties file (application.yml).

#### NOTE:

For the application to work successfully, download a custom JSON parser from the repository
at https://github.com/nikstannum/clevertec-task5-json-parser. Switch to the lib branch and publish it to the local
repository.