Requirements:
- Java 8;
- Maven.

Setup intructions:

1. Download the project from github: https://github.com/Rodrigo-Hackbarth/diff

2. Open the command prompt;

3. Via the command prompt, navigate to the the Diff project folder where the pom file is located;

4. Create the executable JAR file using the command: mvn clean install;

5. Start the application using command: java -jar target/diff.jar;

6. Now the application is up and running and it should be possible to:

    6.1. Save files to be compared sending POST requests to the following endpoints:
    - http://localhost:8080/v1/diff/\<integer id\>/left;
    - http://localhost:8080/v1/diff/\<integer id\>/right;

    Notice that the POST requests have to be configured to send JSON formatted content and the request body has to provide a Base64 encoded file associated to the "file" key, as following: `{"file":"<encoded file content>"}`.

    The response from these requests will also be in JSON format, consisting of "status" and "message" information.

    6.2 Compare two files provided under the same ID, sending a GET request to the following endpoint:
    - http://localhost:8080/v1/diff/\<integer id\>

    Notice that for 2 files to be successfully compared using this GET request, you need to save 2 files using each of the POST requests mentioned in step 6.1 and only then make a GET request to the endpoint metioned in step 6.2 using the same ID in all requests.

    If no file or only one of the files has been provided, the returned JSON will be as following:
    ```json
    {
      "status": "error",
      "message": "<error message>"
    }
    ```
    If both files were provided, are equal in size and have different content, the returned JSON will be as following:
    ```json
    {
      "status": "success",
      "message": null,
      "diffs": [
        {
          "offset": <integer diff offset>,
          "length": <integer diff length>
        }
      ]
    }
    ```
    If both files were provided and they are equal in content or are different in size, the returned JSON will be as following:
    ```json
    {
      "status": "success",
      "message": "<message informing the files are equal or that they are diffent in size>",
      "diffs": []
    }
    ```