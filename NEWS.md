## v4.1.0 YYYY-mm-DD - In progress
### Breaking changes
* Description ([ISSUE](https://folio-org.atlassian.net/browse/ISSUE))

### New APIs versions
* Provides `API_NAME vX.Y`
* Requires `API_NAME vX.Y`

### Features
* Description ([ISSUE](https://folio-org.atlassian.net//browse/ISSUE))

### Bug fixes
* Do not delete kafka topics if collection topic is enabled ([FST-77](https://folio-org.atlassian.net/browse/FST-77))

### Tech Dept
* Description ([ISSUE](https://folio-org.atlassian.net/browse/ISSUE))

### Dependencies
* Bump `LIB_NAME` from `OLD_VERSION` to `NEW_VERSION`
* Add `LIB_NAME` `VERSION`
* Remove `LIB_NAME`

## v4.0.0 2024-03-18
### Breaking changes
* Remove system-user related functionality from "folio-service-tools-spring-dev" ([FST-68](https://issues.folio.org/browse/FST-68))
* Delete TestSetUpHelper.getPgClient() method ([FST-73](https://issues.folio.org/browse/FST-73))
* Remove "folio-service-tools-spring-test" submodule ([FST-74](https://issues.folio.org/browse/FST-74))

### Bug fixes
* Avoid additional totalRecords SQL query for offset=0 limit=MAX_INT ([FST-72](https://issues.folio.org/browse/FST-72))

### Tech Debt
* Spring Boot 3.1.5, folio-spring-base 7.2.2, Snakeyaml, Snappy ([FST-70](https://issues.folio.org/browse/FST-70))

### Dependencies
* Bump `Spring Boot` from `3.1.4` to `3.2.3`
* Bump `folio-spring-base` from `7.2.0` to `8.1.0`
* Bump `log4j` from `2.19.0` to `2.23.1`
* Bump `vertx` from `4.4.5` to `4.5.5`
* Bump `raml-module-builder` from `35.1.0` to `35.2.0`

## v3.1.0 2023-10-10
### Features
* Add userId to FolioExecutionContext for system-user ([FST-46](https://issues.folio.org/browse/FST-46))
* Add common Kafka message producer ([FST-48](https://issues.folio.org/browse/FST-48))
* Add functionality for Kafka topic deleting ([FST-59](https://issues.folio.org/browse/FST-59))
* Add tenant identifying header & add tenant collection topics

### Bug fixes 
* Use lastname property for lastname in system-user creation ([FST-44](https://issues.folio.org/browse/FST-44))

### Tech Dept
* Fix dependencies and failing tests after migrating to Java 17([FST-58](https://issues.folio.org/browse/FST-58))

### Dependencies
* Bump `vertx` from `4.3.8` to `4.4.5`
* Bump `raml-module-builder` from `35.0.5` to `35.1.0`
* Bump `spring-boot` from `3.0.2` to `3.1.4`
* Bump `folio-spring-base` from `6.0.1` to `7.2.0`
* Bump `mod-configuration-client` from `5.9.0` to `5.9.2`
* Bump `commons-io` from `2.11.0` to `2.14.0`

## v3.0.0 2023-02-10
* FST-32 Migrate to Java 17
* FST-32 Implement Kafka topic creation
* FST-34 Implement mechanism of system user creation
* FST-35 Add batch message processor with fallback
* FST-36 Add DatabaseCleanup extension for tests
* FST-37 Migration Spring boot v3
* FST-40 RMB 35.0.5, Vert.x 4.3.7
* FST-41 DbUtils transaction: Explain deprecation, fix unit tests

## v1.10.0 2022-10-18
* FST-29: Vertx 4.3.3, RMB 34.0.2, Wiremock 2.34.0 fixing vulnerabilities
* FST-30: Upgrade to RAML Module Builder 35.0.0

## v1.9.0 2022-06-17
* FST-19: Upgrade to RAML Module Builder 34.0.0
* FST-27: Publish javadoc and sources to maven repository
* FST-24: jackson-databind 2.13.2.1 Denial of Service (CVE-2020-36518)

## v1.8.0 2022-02-23
* FST-19: Upgrade to RAML Module Builder 33.2.6
* FST-21: wiremock-jre8:2.32.0 fixing security vulnerabilities
* FST-22: Update log4j-slf4j-impl from 2.16.0 to 2.17.1

## v1.7.2 2021-12-16
* FST-17: Kiwi R3 2021 - Log4j vulnerability verification and correction

## v1.7.1 2021-10-04
* FST-15: Upgrade folio-service-tools to RMB 33.1.1 and Vert.x 4.1.4
* FST-16: Upgrade folio-service-tools to mod-configuration-client 5.7.1

## v1.7.0 2021-06-09
* FST-6 Upgrade to RMB 33 and Vert.x 4.1.0.CR1

## v1.6.1 2021-01-08
* FST-6 Upgrade to RMB 32.x and Vert.x 4.0
* Update TestSetUpHelper to wait until tenant deployed

## v1.6.0 2020-10-05
* FST-3 Migrate to JDK 11 and RMB 31.x
* Restore method to create db params as json array
* Honor existing postgres in favor of embedded one

## v1.5.1 2020-06-12
* Add util methods to RowSetUtils
* Update to RMB v30.0.2 and Vert.X 3.9.1

## v1.5.0 2020-06-07
* Bump up minor version

## v1.4.4 2020-06-07
* FST-1 Update to RMB v30.0.0 and Vert.X 3.9.0
* MODCFIELDS-49 Add util classes

## v1.4.3 2020-05-18
* MODKBEKBJ-432 Convert headers to case insensitive in TokenUtils.fetchUserInfo()

## v1.4.2 2020-05-06
* MODKBEKBJ-432 Add fetchUserInfo(token) method
* MODKBEKBJ-432 Move TokenUtils/FutureUtils from mod-kb-ebsco-java

## v1.4.1 2020-02-24
* introduce invalid data exceptions

## v1.4.0 2020-01-28    
* Add code owners 
* MODCFIELDS-27 - Move utility methods from mod-kb-ebsco-java

## v1.3.1 2020-01-02    
* remove explicit dependency to okapi-common 
* update RMB to 29.1.1

## v1.3.0 2019-11-27    
* MODKBEKBJ-339 - Update RMB to 29.0.1

## v1.2.0 2019-08-21
* MODCFIELDS-9 - Add base class for tests that use combination of Vert.x, Wiremock and Postgres servers

## v1.1.0 2019-07-23
* MODNOTES-100 - Fix error message when we try to delete a note type
* MODNOTES-106 - Provide a way to convert generic database exceptions into more specific ones
* MODNOTES-104 - Refactoring: split NoteLinksImpl into fine grained REST / Service / Repository parts

## v1.0.1 2019-06-19
* FOLIO-2106 - Update folio-service-tools to RMB25

## v1.0.0 2019-05-23
* initial library release
* configuration retrieval tool
* REST exception handling
