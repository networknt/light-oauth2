# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [Unreleased]
### Added

### Changed

## 1.3.1 - 2017-06-03
### Added

### Changed
- Update Dockerfile to reflect the version change. Thanks @gonzalovazquez
- Fixes #24 createDt and updateDt not populated in in-memory data grid
- Fixes #23 default allowed origin in CORS to http://localhost:8080 for market place development. 
- Fixes #25 return clear text client secret after creating a new client
- Fixes #26 Oracle XE vesion to 16.04 in docker-compose-oracle.yml
- Fixes #27 remove client secret for all get endpoints for client
- Fixes #28 all client authenticated by form data Thanks @smerschjohann
- Fixes #32 add one more test case to ensure that one of the scope matches with multiple scopes
- Fixes #35 upgrade light-4j and light-rest-4j to 1.3.1

## 1.2.4 - 2017-02-19
### Added

### Changed
- Remove development edition and replace it with seven microservices for enterprise edition
- Upgrade to framework 1.2.4


## 0.1.2 - 2016-10-10
### Added
- Test cases

### Changed
- Update exception handling with ApiException
- Upgrade to framework 0.1.7
- Remove several config files that are not used
- Add status.json config for additional codes for OAuth2


## 0.1.1 - 2016-09-19
### Added

### Changed
- Upgrade to framework 0.1.2


## 0.1.0 - 2016-08-16
### Added
- Code Handler
- Token Handler
