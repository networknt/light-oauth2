DROP DATABASE IF EXISTS oauth2;
CREATE DATABASE oauth2;

GRANT ALL PRIVILEGES ON oauth2.* TO 'mysqluser'@'%' WITH GRANT OPTION;

USE oauth2;

DROP TABLE IF EXISTS client_service;
DROP TABLE IF EXISTS service_endpoint;
DROP TABLE IF EXISTS service;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS user_profile;
DROP TABLE IF EXISTS audit_log;


CREATE TABLE user_profile (
  user_id VARCHAR(32) NOT NULL,
  user_type VARCHAR(16) NOT NULL,  -- admin, customer, employee, partner
  first_name VARCHAR(32) NOT NULL,
  last_name VARCHAR(32) NOT NULL,
  email VARCHAR(64) NOT NULL,
  password VARCHAR(1024) NOT NULL,
  roles VARCHAR(2048), -- space delimited roles
  PRIMARY KEY (user_id)
)
ENGINE=INNODB;

CREATE UNIQUE INDEX email_idx ON user_profile(email);


CREATE TABLE client (
  client_id VARCHAR(36) NOT NULL,
  client_type VARCHAR(12) NOT NULL,  -- public, confidential, trusted
  client_profile VARCHAR(10) NOT NULL, -- webserver, mobile, browser, batch, service
  client_secret VARCHAR(1024) NOT NULL,
  client_name VARCHAR(32) NOT NULL,
  client_desc VARCHAR(2048),
  scope VARCHAR(4096),
  custom_claim VARCHAR(4096), -- custom claim(s) in json format that will be included in the jwt token
  redirect_uri VARCHAR(1024),
  authenticate_class VARCHAR(256),
  owner_id VARCHAR(32) NOT NULL,
  PRIMARY KEY (client_id),
  FOREIGN KEY (owner_id) REFERENCES user_profile(user_id)
)
ENGINE=INNODB;


CREATE TABLE service (
  service_id VARCHAR(32) NOT NULL,
  service_type VARCHAR(16) NOT NULL,  -- swagger, openapi, graphql, hybrid
  service_name VARCHAR(32) NOT NULL,
  service_desc VARCHAR(1024),
  scope VARCHAR(1024),
  owner_id VARCHAR(32) NOT NULL,
  PRIMARY KEY (service_id),
  FOREIGN KEY (owner_id) REFERENCES user_profile(user_id)
)
ENGINE=INNODB;


CREATE TABLE service_endpoint (
  service_id VARCHAR(32) NOT NULL,
  endpoint VARCHAR(256) NOT NULL,  -- different framework will have different endpoint format.
  operation VARCHAR(256) NOT NULL,
  scope VARCHAR(64) NOT NULL,
  PRIMARY KEY (service_id, endpoint),
  FOREIGN KEY (service_id) REFERENCES service(service_id)
)
ENGINE=INNODB;


CREATE TABLE client_service (
  client_id VARCHAR(36) NOT NULL,
  service_id VARCHAR(32) NOT NULL,
  endpoint VARCHAR(256) NOT NULL,  -- different framework will have different endpoint format.
  PRIMARY KEY (client_id, service_id, endpoint),
  FOREIGN KEY (service_id, endpoint) REFERENCES service_endpoint(service_id, endpoint),
  FOREIGN KEY (client_id) REFERENCES client(client_id)
)
ENGINE=INNODB;

create table audit_log (
  log_id INT, -- system milliseonds from 1970.
  service_id VARCHAR(32) NOT NULL,
  endpoint VARCHAR(256) NOT NULL,
  request_header VARCHAR(4096),
  request_body VARCHAR(4096),
  response_code INT,
  response_header VARCHAR(4096),
  response_body VARCHAR(4096)
)
ENGINE=INNODB;

/*
CREATE TABLE IF NOT EXISTS client (
  client_id VARCHAR(32) NOT NULL,
  client_secret VARCHAR(512) NOT NULL,
  client_type VARCHAR(12) NOT NULL, -- public confidential trusted
  client_description VARCHAR(1024),
  reuse_refresh_tokens BOOLEAN DEFAULT true NOT NULL,
  dynamically_registered BOOLEAN DEFAULT false NOT NULL,
  allow_introspection BOOLEAN DEFAULT false NOT NULL,
  id_token_validity_seconds BIGINT DEFAULT 600 NOT NULL,
  access_token_validity_seconds BIGINT,
  refresh_token_validity_seconds BIGINT,

  response_types VARCHAR(256),  -- response type values that the client is declaring that it will use. separated by space
  grant_type VARCHAR(256), -- grant types that the client is declaring it will use. separated by space
  application_type VARCHAR(256), -- kind of the application native or web. If omitted, web is used as the default.
  client_name VARCHAR(255), -- name of the client to be presented to the end-user on the log in page.
  logo_uri VARCHAR(256), -- url that references a logo for the client application
  policy_uri VARCHAR(256),  -- url that the relying party client provides to the end-user to read about how the profile data will be used.
  client_uri VARCHAR(256),  -- url of the home page of the client
  tos_uri VARCHAR(256),  -- url that the relying party client provides to the end-user to read about the client's terms of service
  jwks_uri VARCHAR(256),  -- url for the client's json web key set document
  jwks TEXT, -- client's json web key set document, passed by value
  sector_identifier_uri VARCHAR(256),  -- url using the https schema to be used in calculating pseudonymous identifiers by the OP
  subject_type VARCHAR(256),  -- subject type requested for responses to this client

  id_token_signed_response_alg VARCHAR(256), -- jws alg algorithm required for signing the id token issued to this client
  id_token_encrypted_response_alg VARCHAR(256),  -- jwe algorithm required for encrypting the id token issued to this client
  id_token_encrypted_response_enc VARCHAR(256),  -- jwe enc algorithm required for encrypting the id token issued to this client

  user_info_signed_response_alg VARCHAR(256), -- jws alg algorithm required for signing userinfo response
  user_info_encrypted_response_alg VARCHAR(256),  -- jwe alg algorithm required for encrypting userinfo response
  user_info_encrypted_response_enc VARCHAR(256), -- jwe enc algorithm required for encrypting userinfo response

  request_object_signing_alg VARCHAR(256), -- jws alg algorithm that must be used for signing request objects sent to the OP
  request_object_encryption_alg VARCHAR(256), -- jwe alg algorithm that RP is declaring that it may use for encrypting request object sent to the OP
  request_object_encryption_enc VARCHAR(256), -- jwe enc algorithm the RP is declaring that it may use for encrypting request object sent to the OP

  token_endpoint_auth_method VARCHAR(256), -- requested client authentication method for the token endpoint
  token_endpoint_auth_signing_alg VARCHAR(256),  -- WS alg algorithm that must be used for signing the JWT used to authenticate the Client at the Token Endpoint for the private_key_jwt and client_secret_jwt authentication methods

  default_max_age BIGINT, -- Default Maximum Authentication Age
  require_auth_time BOOLEAN, -- Boolean value specifying whether the auth_time Claim in the ID Token is required.
  default_acr_values VARCHAR(256), -- Default requested Authentication Context Class Reference values.
  initiate_login_uri VARCHAR(256), -- URI using the https scheme that a third party can use to initiate a login by the RP.

  redirect_uris VARCHAR(1024),  -- redirect URI values used by the client, may be multiple values separated by space.

  created_dt DATE NOT NULL,
  owner_id VARCHAR(32) NOT NULL, -- link to user entity by id for contact.

  PRIMARY KEY (client_id)
)
ENGINE=INNODB;
*/


INSERT INTO user_profile(user_id, user_type, first_name, last_name, email, roles, password)
VALUES('admin', 'admin', 'admin', 'admin', 'admin@networknt.com', 'user admin', '1000:5b39342c202d37372c203132302c202d3132302c2034372c2032332c2034352c202d34342c202d31362c2034372c202d35392c202d35362c2039302c202d352c202d38322c202d32385d:949e6fcf9c4bb8a3d6a8c141a3a9182a572fb95fe8ccdc93b54ba53df8ef2e930f7b0348590df0d53f242ccceeae03aef6d273a34638b49c559ada110ec06992');

INSERT INTO client (client_id, client_secret, client_type, client_profile, client_name, client_desc, scope, custom_claim, redirect_uri, owner_id)
VALUES('f7d42348-c647-4efb-a52d-4c5787421e72', '1000:5b37332c202d36362c202d36392c203131362c203132362c2036322c2037382c20342c202d37382c202d3131352c202d35332c202d34352c202d342c202d3132322c203130322c2033325d:29ad1fe88d66584c4d279a6f58277858298dbf9270ffc0de4317a4d38ba4b41f35f122e0825c466f2fa14d91e17ba82b1a2f2a37877a2830fae2973076d93cc2', 'public', 'mobile', 'PetStore Web Server', 'PetStore Web Server that calls PetStore API', 'petstore.r petstore.w', '{"c1": "361", "c2": "67"}', 'http://localhost:8080/authorization', 'admin');

INSERT INTO service (service_id, service_type, service_name, service_desc, scope, owner_id)
VALUES ('AACT0001', 'openapi', 'Account Service', 'A microservice that serves account information', 'a.r b.r', 'admin');
