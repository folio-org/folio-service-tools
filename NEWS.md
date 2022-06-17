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
