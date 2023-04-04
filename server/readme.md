# Analysis & Development Project (Atium robots) - Mars 2052 - server project

## Summary

This is the **server side project** for Atium robots.

The server provides everything you need to run the POC.

You can check the API spec files provided in this document to check the functionality of the server.

The server contains endpoints used to create, delete bookings. Check a drone ride in realtime and ask for information about rides and bookings.

We also provided a vertx rtc implementation to simulate a drone ride.

In this file you can find information on how to run and test the server.

## Before you start

- Choose Zulu jdk version 11 or opendjk 11 (Configure through this through intelij)
- Make sure to clone **all** the repositories **client**, **server** & **documentation**
  - **Use the following folder structure**
  - root_folder_with_name_of_choice
    - client
    - documentation
    - server

## Local CI testing

You can **run** the Sonar validator **locally!**

There is no need to push to the server to check if you are compliant with our rules. In the interest of sparing the
server, please result to local testing as often as possible.

**If everyone pushes to test, the remote will not last.**

To run the analysis open the gradle tab (right side, with the elephant icon), open Tasks, open verification and run (
double click)
the task **test** then **jacocoTestCoverageVerification** then **jacocoTestReport** then **sonarqube**.

The results should now be available on the public sonar. (see section production endpoints)

## What's included

- The openAPI specification
  - localhost:8080/API/drones
  - localhost:8080/API/bookings
  - localhost:8080/API/rides
- H2 database web console
  - The setup of a vert.x and openAPI (WebServer.java)
- H2 repository class
  - The RTC to track a droneride (MarsRtcBridge.java)
  - Database maintain scripts

## How to run the start project locally

In Intelij choose gradle task **run**.

- Make sure to implement the folder structure as described in section **before you start**.
  - Otherwise, Vert.x will not find the openAPI specification.

## Location OpenAPI Specification

The location of the openAPI specification is defined in the file **config**.

The property is called **API.url**.

By default, the local setup will pick the openAPI specification located in the **documentation** repository in the
folder **API-spec**.

As mentioned before, it's very important to implement the correct folder structure.

If for some reason you want to use another openAPI specification, please let the property **API.url** point the correct
specification. Don't forget to also change the **config file** in the test resources. This property allows relative and
absolute paths.

By default this property is assigend the value:

```json
"API": {
    "url": "../documentation/API-spec/openAPI-mars.yaml"
  }
```

## Local endpoints

- H2 web client
  - localhost:9000
  - url: ~/mars-db
  - no credentials
- Web API
  - localhost:8080/API/drones
  - localhost:8080/API/bookings
  - localhost:8080/API/rides
- Web client
  - launch through webstorm/phpstorm (see client-side readme)

## Production endpoints

- H2 web client
  - <https://project-ii.ti.howest.be/db-03>
  - url: jdbc:h2:/opt/group-03/db-03
  - username:group-03
  - password: X/ebV6/U3ac
- Useful information
  - Server logs
    - <https://project-ii.ti.howest.be/monitor/logs/group-03>
  - Swagger Interface
        - <https://project-ii.ti.howest.be/monitor/swagger/group-03>
        - Through this GUI remote & local API testing is possible!
- Web client project
  - <https://project-ii.ti.howest.be/mars-03>
- Sonar
  - <https://sonar.ti.howest.be/dashboard?id=2021.project-ii%3Amars-server-03>
  - <https://sonar.ti.howest.be/dashboard?id=2021.project-ii%3Amars-client-03>

## Keep the database up to date

There is no need to manually add entries into the database.

All the sample data you need to run the server is already provided in the database scripts that get executed when running the server.

Please use the scripts: **db-create** and **db-populate** in the resource folder.

The **db-create** script is responsible for create the database structure (tables, primary keys, ...)

The **db-populate** script is responsible for populating the database with useful data.
