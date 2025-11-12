# folio-service-tools

[![FOLIO](https://img.shields.io/badge/FOLIO-Library-green)](https://www.folio.org/)
[![Release Version](https://img.shields.io/github/v/release/folio-org/folio-service-tools?sort=semver&label=Latest%20Release)](https://github.com/folio-org/folio-service-tools/releases)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.folio%3Afolio-service-tools&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=org.folio%3Afolio-service-tools)
[![Java Version](https://img.shields.io/badge/Java-21-blue)](https://openjdk.org/projects/jdk/21/)

Copyright © 2019–2025 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Introduction

General purpose library to help with FOLIO backend service development and testing. This library provides utilities for database operations, configuration management, service exception handling, and testing support for both Vert.x and Spring-based FOLIO modules.

<!-- TOC -->
* [folio-service-tools](#folio-service-tools)
  * [Introduction](#introduction)
  * [Modules](#modules)
  * [Requirements](#requirements)
  * [Key Features](#key-features)
    * [Database Utilities (folio-service-tools-dev)](#database-utilities-folio-service-tools-dev)
    * [Configuration Management](#configuration-management)
    * [Service Exception Handling](#service-exception-handling)
    * [Testing Support (folio-service-tools-test)](#testing-support-folio-service-tools-test)
    * [Spring Integration (folio-service-tools-spring-dev)](#spring-integration-folio-service-tools-spring-dev)
  * [Additional Information](#additional-information)
    * [Issue tracker](#issue-tracker)
    * [Code analysis](#code-analysis)
    * [Contributing](#contributing)
<!-- TOC -->

## Modules

This project consists of three modules:

- **folio-service-tools-dev** - Core utilities for database operations, configuration, REST utilities, and service exception handling
- **folio-service-tools-test** - Testing utilities and helpers for FOLIO backend services
- **folio-service-tools-spring-dev** - Spring Framework integration support for FOLIO modules

## Requirements

- **Java:** 21+
- **Vert.x:** 5.0.x+
- **Spring Boot:** 3.5.x+ (for spring-dev module)
- **RAML Module Builder:** 36.0.x+

## Key Features

### Database Utilities (folio-service-tools-dev)

- **DbUtils** - Database query execution and transaction helpers
- **CqlQuery** - CQL (Contextual Query Language) query building and parsing
- **RowSetUtils** - Utilities for working with Vert.x SQL client RowSet results
- **PostgreSQL Exception Translation** - Automatic translation of PostgreSQL errors to meaningful exceptions

### Configuration Management

- **ModConfiguration** - Read configuration from mod-configuration service
- Support for typed configuration values (String, Integer, Long, Double, Boolean)
- Default value fallback support

### Service Exception Handling

- Partial functions for functional error handling
- Standardized exception types for common service errors
- Database exception translation to business exceptions

### Testing Support (folio-service-tools-test)

- Testing utilities for Vert.x-based services
- WireMock integration for mocking external services
- PostgreSQL testing support via RMB
- REST-assured utilities for API testing

### Spring Integration (folio-service-tools-spring-dev)

- Kafka consumer/producer configuration and support
- Spring-based service development utilities
- Integration with FOLIO Spring base

## Additional Information

For more FOLIO developer documentation, visit [dev.folio.org](https://dev.folio.org/)

### Issue tracker

See project [FST](https://folio-org.atlassian.net/browse/FST)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker/).

### Code analysis

[SonarQube analysis](https://sonarcloud.io/project/overview?id=org.folio:folio-service-tools).

### Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.
