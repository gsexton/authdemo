
## Requirements

Interview coding exercise

In the language of your choice, create a user authentication program. In this program, a user should be able to register an account with secure login credentials, securely login to that account using those credentials, and logout.

Requirements
* Please do not take more than 8 hours to develop this solution.
* The code should be posted to an accessible location, such as GitHub, GitLab, BitBucket, or similar.
* Instructions on how to build and run the program, or a demo of the running program, is provided.
* Credential storage must be local
* (UI characteristics are not important for this exercise, so please feel free to spend only the necessary amount of time on this portion of the program. Perhaps consider a CLI)

What we will evaluate
* Your understanding of secure code characteristics
* Proof that your code works
* Your approach to solution design and coding best practices
* Your coding-style

## Requirements

* Apache Maven - I used 3.8.4
* JDK - I used Azul JDK 1.8 build 312.

## Compiling

From the top-level directory, execute the command:

```mvn package```

## Executing the code:

From the top-level directory, execute the commands:

```
mvn package
java -jar target/authdemo-jar-with-dependencies.jar --help
```

## Debugging the Code

To attach a debugger, start the program as shown below, and connect a Java debugger to localhost:

```
java -Xrs -Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8008 -Djava.compiler=NONE -Djava.awt.headless=true -jar target/authdemo-jar-with-dependencies.jar --help
    
```

## Unit Tests

Tests are created using Cucumber/Gherkin. You can see the feature file in src/test/resources/authdemo/operation\_test.feature.

These tests actually exercise the classes, and not the CLI since the CLI is just a test harness for the classes anyhow.

The tests will run as part of the package target or directly as the test target.

```
mvn package
mvn test
```

## Running Static Code Analyzer

I added the static source code analyzer PMD to the project. It will list unused imports, variables, etc. 
To use it, from the top-level directory, execute the command:

```mvn site```

Output will be in the target/site directory in a file named pmd.html. View it with a browser.

## Requirements Response

* I really spent 9 hours. I got to 8 hours and didn't have any tests so I spent another hour creating them.
* Instructions on build and run provided.
* Credential storage will be in a local file named account-info.yaml which will be in the same directory the program is executed from. It's using SnakeYAML for serialization, but the default seems to be a weird yaml/JSON hybrid format. If I had more time, I'd figure out if I could make it prettier...
* UI Characteristics. Fully CLI. --help will print the implemented commands.


