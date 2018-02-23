# Change Log

## [1.5.9](https://github.com/networknt/light-oauth2/tree/1.5.9) (2018-02-21)
[Full Changelog](https://github.com/networknt/light-oauth2/compare/1.5.8...1.5.9)

**Closed issues:**

- cascade delete service endpoints if the service is deteted [\#72](https://github.com/networknt/light-oauth2/issues/72)
- update mysql script and service.yml to use mysqluser instead of root [\#71](https://github.com/networknt/light-oauth2/issues/71)

**Merged pull requests:**

- fixes \#52 - update Dockerfile-Redhat files in all services [\#70](https://github.com/networknt/light-oauth2/pull/70) ([DineshAlapati](https://github.com/DineshAlapati))

## [1.5.8](https://github.com/networknt/light-oauth2/tree/1.5.8) (2018-02-03)
[Full Changelog](https://github.com/networknt/light-oauth2/compare/1.5.7...1.5.8)

**Fixed bugs:**

- Fix docker-compose build [\#57](https://github.com/networknt/light-oauth2/issues/57)

**Closed issues:**

- uppgrade to Hazelcast 2.9.2 [\#66](https://github.com/networknt/light-oauth2/issues/66)
- update db scripts and swagger.json for mysql, postgres and oracle [\#65](https://github.com/networknt/light-oauth2/issues/65)
- update README.md with more information and links to doc site [\#64](https://github.com/networknt/light-oauth2/issues/64)
- add client to service relationship API [\#62](https://github.com/networknt/light-oauth2/issues/62)
- add service endpoints API [\#61](https://github.com/networknt/light-oauth2/issues/61)
- refactor table names before service enhancement [\#60](https://github.com/networknt/light-oauth2/issues/60)
- externalize config files to db directory [\#59](https://github.com/networknt/light-oauth2/issues/59)
- Dockerfile for production images for all services [\#52](https://github.com/networknt/light-oauth2/issues/52)

**Merged pull requests:**

- fixes \#52 - update user permissions on artifacts in production docker… [\#67](https://github.com/networknt/light-oauth2/pull/67) ([DineshAlapati](https://github.com/DineshAlapati))

## [1.5.7](https://github.com/networknt/light-oauth2/tree/1.5.7) (2018-01-01)
[Full Changelog](https://github.com/networknt/light-oauth2/compare/1.5.6...1.5.7)

## [1.5.6](https://github.com/networknt/light-oauth2/tree/1.5.6) (2017-12-31)
[Full Changelog](https://github.com/networknt/light-oauth2/compare/1.4.3...1.5.6)

**Closed issues:**

- update .gitignore to ignore dependency-reduced-pom.xml [\#56](https://github.com/networknt/light-oauth2/issues/56)
- remove dependency-reduced-pom.xml for each sub project [\#55](https://github.com/networknt/light-oauth2/issues/55)
- upgrade secret.yml to 1.5.6 with emailPassword [\#54](https://github.com/networknt/light-oauth2/issues/54)
- remove default config for production package [\#53](https://github.com/networknt/light-oauth2/issues/53)
- Update readme with links to document site and remove docs folder [\#50](https://github.com/networknt/light-oauth2/issues/50)
- Upgrade docker-compose files and db configurations to 1.5.4 [\#49](https://github.com/networknt/light-oauth2/issues/49)
- Upgrade dependencies and add maven-version [\#48](https://github.com/networknt/light-oauth2/issues/48)

**Merged pull requests:**

- \#49: upgrade docker-compose files and db configurations to 1.5.4 [\#51](https://github.com/networknt/light-oauth2/pull/51) ([DineshAlapati](https://github.com/DineshAlapati))

## [1.4.3](https://github.com/networknt/light-oauth2/tree/1.4.3) (2017-09-10)
[Full Changelog](https://github.com/networknt/light-oauth2/compare/1.4.2...1.4.3)

## [1.4.2](https://github.com/networknt/light-oauth2/tree/1.4.2) (2017-08-31)
[Full Changelog](https://github.com/networknt/light-oauth2/compare/1.4.1...1.4.2)

**Closed issues:**

- Upgrade all test cases to https and http2 [\#47](https://github.com/networknt/light-oauth2/issues/47)

## [1.4.1](https://github.com/networknt/light-oauth2/tree/1.4.1) (2017-08-31)
[Full Changelog](https://github.com/networknt/light-oauth2/compare/1.4.0...1.4.1)

**Closed issues:**

-  Make all services HTTP2 and HTTPS enabled and disable HTTP by default [\#46](https://github.com/networknt/light-oauth2/issues/46)
- Upgrade to newer version of Undertow and Jackson [\#45](https://github.com/networknt/light-oauth2/issues/45)

## [1.4.0](https://github.com/networknt/light-oauth2/tree/1.4.0) (2017-08-23)
[Full Changelog](https://github.com/networknt/light-oauth2/compare/1.3.4...1.4.0)

**Closed issues:**

- Replace Client with Http2Client and remove dependency of apache httpclient [\#44](https://github.com/networknt/light-oauth2/issues/44)
- Update the dependency on security module TokenHelper to OauthHelper [\#43](https://github.com/networknt/light-oauth2/issues/43)
- Upgrade to Undertow 1.4.18.Final for Http2 and remove JsonPath dependency [\#42](https://github.com/networknt/light-oauth2/issues/42)
- Update password match from String to char\[\] to prevent revealing password with JVM heap dump [\#41](https://github.com/networknt/light-oauth2/issues/41)

## [1.3.4](https://github.com/networknt/light-oauth2/tree/1.3.4) (2017-07-09)
[Full Changelog](https://github.com/networknt/light-oauth2/compare/1.3.1...1.3.4)

**Implemented enhancements:**

- Implement PKCE extension for authorization code flow for mobile native apps [\#29](https://github.com/networknt/light-oauth2/issues/29)
- Implement Open ID Connect on top of the current OAuth2 Authorization Server enterprise edition [\#15](https://github.com/networknt/light-oauth2/issues/15)

**Closed issues:**

- Add build.sh to automatically build, tag and push to docker hub for each service [\#40](https://github.com/networknt/light-oauth2/issues/40)
- Implement custom grant type client\_authenticated\_user [\#39](https://github.com/networknt/light-oauth2/issues/39)
- Pass externalized logback.xml in Dockerfile for all services [\#37](https://github.com/networknt/light-oauth2/issues/37)
- Inject server/info and /health into swagger for oauth2 services. [\#36](https://github.com/networknt/light-oauth2/issues/36)

## [1.3.1](https://github.com/networknt/light-oauth2/tree/1.3.1) (2017-06-03)
[Full Changelog](https://github.com/networknt/light-oauth2/compare/1.2.4...1.3.1)

**Implemented enhancements:**

- Refactor development edition to use the same swagger specification for validation [\#5](https://github.com/networknt/light-oauth2/issues/5)

**Fixed bugs:**

- After client registration, the returned client\_secret is the hashed and salted value not the clear text. [\#25](https://github.com/networknt/light-oauth2/issues/25)
- service registration and retrieval createDt is null in the result. [\#24](https://github.com/networknt/light-oauth2/issues/24)

**Closed issues:**

- Upgrade to framework 1.3.1 [\#35](https://github.com/networknt/light-oauth2/issues/35)
- Add one test case for token service to ensure that one of the scope matches with client with multiple scopes [\#32](https://github.com/networknt/light-oauth2/issues/32)
- Upgrade to the latest framework and config. Also dependencies [\#31](https://github.com/networknt/light-oauth2/issues/31)
- clientSecret hash is leaked on GET request [\#27](https://github.com/networknt/light-oauth2/issues/27)
- Fix oracle XE docker image version to 16.04 instead of latest which is broken [\#26](https://github.com/networknt/light-oauth2/issues/26)
- CORS issue when calling from a SPA [\#23](https://github.com/networknt/light-oauth2/issues/23)
- Receive Unexpected runtime exception when registering a service. [\#21](https://github.com/networknt/light-oauth2/issues/21)

**Merged pull requests:**

- allow client authentication by form data [\#28](https://github.com/networknt/light-oauth2/pull/28) ([smerschjohann](https://github.com/smerschjohann))

## [1.2.4](https://github.com/networknt/light-oauth2/tree/1.2.4) (2017-02-20)
[Full Changelog](https://github.com/networknt/light-oauth2/compare/1.0.0...1.2.4)

**Merged pull requests:**

- updated version on Docker build [\#20](https://github.com/networknt/light-oauth2/pull/20) ([gonzalovazquez](https://github.com/gonzalovazquez))

## [1.0.0](https://github.com/networknt/light-oauth2/tree/1.0.0) (2017-02-20)
[Full Changelog](https://github.com/networknt/light-oauth2/compare/0.1.2...1.0.0)

**Implemented enhancements:**

- Enable CORS support for client, service, user and refresh token in order to support marketplace SPA call directly from browser. [\#19](https://github.com/networknt/light-oauth2/issues/19)
- Implement refresh token for authorization code grant type [\#16](https://github.com/networknt/light-oauth2/issues/16)
- Update development edition to have the exact api like enterprise edition [\#13](https://github.com/networknt/light-oauth2/issues/13)
- Validate token service redirect\_uri exists and is the same as the one passed in code service [\#12](https://github.com/networknt/light-oauth2/issues/12)
- Update redirect\_url to redirect\_uri to follow standard naming in the specification [\#11](https://github.com/networknt/light-oauth2/issues/11)
- Support state in authorization code grant type [\#10](https://github.com/networknt/light-oauth2/issues/10)
- Implement Resource Owner Password Credentials Grant in token service in enterprise edition [\#9](https://github.com/networknt/light-oauth2/issues/9)
- Add more test cases to cover negative case in enterprise edition. [\#8](https://github.com/networknt/light-oauth2/issues/8)
- Add client profile in order to categorize clients along with client type [\#7](https://github.com/networknt/light-oauth2/issues/7)
- Implement HTTPS with openssl which is much faster than JDK [\#6](https://github.com/networknt/light-oauth2/issues/6)
- Client secret needs to be hashed and salted just like user password [\#4](https://github.com/networknt/light-oauth2/issues/4)
- Merge oracle, mysql and postgres branch together [\#3](https://github.com/networknt/light-oauth2/issues/3)

**Fixed bugs:**

- Authorization code grant type scope is passed in code service and retrieved from token service [\#14](https://github.com/networknt/light-oauth2/issues/14)

**Closed issues:**

- Add range search for user and service [\#2](https://github.com/networknt/light-oauth2/issues/2)
- Add pagination to getUser, getClient and getService endpoints in Enterprise edition [\#1](https://github.com/networknt/light-oauth2/issues/1)

## [0.1.2](https://github.com/networknt/light-oauth2/tree/0.1.2) (2016-10-10)


\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*