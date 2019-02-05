# springboot-apikey-example
[![Build Status](https://travis-ci.org/gregwhitaker/springboot-apikey-example.svg?branch=master)](https://travis-ci.org/gregwhitaker/springboot-apikey-example)

An example of authenticating with a Spring Boot application using an API key.

## Prerequisites
This example requires that you have a running [PostgreSQL](https://www.postgresql.org/) database. You can start one as a Docker container using the following commands:

    $ docker pull postgres
    $ docker run -p 5432:5432 postgres

## Running the Example
Follow the steps below to run the example:

1. Ensure you have a running PostgreSQL instance at `localhost:5432`.

2. Run the following command to start the example application:

        ./gradlew bootRun
        
3. Run the following command to send a request to the non-secure endpoint:

        curl -v http://localhost:8080/api/v1/nonsecure
        
    If successful, you will receive an `HTTP 200 OK` response.
    
4. Run the following command to send a request to the secure endpoint:

        curl -v http://localhost:8080/api/v1/secure
        
    You will receive an `HTTP 403 Forbidden` response because you have not supplied a valid API key.
    
5. Run the following command to send a request to the secure endpoint with an API key:

        curl -v --header "API_KEY: aec093c2c98144f99a4a365ad1d2f05e" http://localhost:8080/api/v1/secure
        
    If successful, you will now receive an `HTTP 200 OK` response because you have supplied a valid API key.

## Bugs and Feedback
For bugs, questions, and discussions please use the [Github Issues](https://github.com/gregwhitaker/springboot-apikey-example/issues).

## License
Copyright 2019 Greg Whitaker

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.