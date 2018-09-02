DROP TABLE client_service CASCADE CONSTRAINTS;
DROP TABLE service_endpoint CASCADE CONSTRAINTS;
DROP TABLE service CASCADE CONSTRAINTS;
DROP TABLE client CASCADE CONSTRAINTS;
DROP TABLE user_profile CASCADE CONSTRAINTS;
DROP TABLE audit_log CASCADE CONSTRAINTS;
DROP TABLE refresh_token CASCADE CONSTRAINTS;
DROP TABLE oauth_provider CASCADE CONSTRAINTS;

CREATE TABLE user_profile (
  user_id VARCHAR2(32) NOT NULL,
  user_type VARCHAR2(16) NOT NULL,  -- admin, customer, employee, partner
  first_name VARCHAR2(32) NOT NULL,
  last_name VARCHAR2(32) NOT NULL,
  email VARCHAR2(64) NOT NULL,
  password VARCHAR2(1024) NOT NULL,
  roles VARCHAR2(2048), -- space delimited roles
  CONSTRAINT user_profile_pk PRIMARY KEY (user_id)
);

CREATE UNIQUE INDEX email_idx ON user_profile(email);

CREATE TABLE client (
  client_id VARCHAR2(36) NOT NULL,
  client_type VARCHAR2(12) NOT NULL,  -- public, confidential, trusted, external
  client_profile VARCHAR2(10) NOT NULL, -- webserver, mobile, browser, service, batch
  client_secret VARCHAR2(1024) NOT NULL,
  client_name VARCHAR2(32) NOT NULL,
  client_desc VARCHAR2(2048),
  scope VARCHAR2(4000),
  custom_claim VARCHAR2(4000), -- custom claim(s) in json format that will be included in the jwt token
  redirect_uri VARCHAR2(1024),
  authenticate_class VARCHAR2(256),
  deref_client_id VARCHAR2(36), -- only this client calls AS to deref token to JWT for external client type
  owner_id VARCHAR2(32) NOT NULL,
  CONSTRAINT client_pk PRIMARY KEY (client_id),
  CONSTRAINT client_user_fk
    FOREIGN KEY (owner_id)
    REFERENCES user_profile(user_id)
);

CREATE TABLE service (
  service_id VARCHAR2(32) NOT NULL,
  service_type VARCHAR2(16) NOT NULL,  -- swagger, openapi, graphql, hybrid
  service_name VARCHAR2(32) NOT NULL,
  service_desc VARCHAR2(1024),
  scope VARCHAR2(1024),
  owner_id VARCHAR2(32) NOT NULL,
  CONSTRAINT service_pk PRIMARY KEY (service_id),
  CONSTRAINT service_user_fk
    FOREIGN KEY (owner_id)
    REFERENCES user_profile(user_id)
);

CREATE TABLE service_endpoint (
  service_id VARCHAR2(32) NOT NULL,
  endpoint VARCHAR2(256) NOT NULL,  -- different framework will have different endpoint format.
  operation VARCHAR2(256) NOT NULL,
  scope VARCHAR2(64) NOT NULL,
  CONSTRAINT service_endpoint_pk PRIMARY KEY (service_id, endpoint),
  CONSTRAINT service_endpoint_service_fk FOREIGN KEY (service_id) REFERENCES service(service_id)
);

CREATE TABLE client_service (
  client_id VARCHAR2(36) NOT NULL,
  service_id VARCHAR2(32) NOT NULL,
  endpoint VARCHAR2(256) NOT NULL,  -- different framework will have different endpoint format.
  CONSTRAINT client_service_pk PRIMARY KEY (client_id, service_id, endpoint),
  CONSTRAINT client_service_endpoint_fk FOREIGN KEY (service_id, endpoint) REFERENCES service_endpoint(service_id, endpoint),
  CONSTRAINT client_service_client_fk FOREIGN KEY (client_id) REFERENCES client(client_id)
);

CREATE TABLE refresh_token (
  user_id VARCHAR2(36) NOT NULL,
  user_type VARCHAR2(36),
  roles VARCHAR2(2048),
  client_id VARCHAR2(36) NOT NULL,
  scope VARCHAR2(64) NOT NULL,
  refresh_token VARCHAR2(256) NOT NULL,
  CONSTRAINT refresh_token_pk PRIMARY KEY (client_id, refresh_token),
  CONSTRAINT refresh_token_client_fk FOREIGN KEY (client_id) REFERENCES client(client_id)
);

CREATE TABLE oauth_provider (
  provider_id VARCHAR2(2) NOT NULL,
  server_url VARCHAR2(256) NOT NULL,  -- different framework will have different endpoint format.
  uri VARCHAR2(64) NOT NULL,
  provider_name VARCHAR2(64),
  CONSTRAINT provider_pk PRIMARY KEY (provider_id)
);

create table audit_log (
  log_id numeric, -- system milliseonds from 1970.
  service_id VARCHAR2(32) NOT NULL,
  endpoint VARCHAR2(256) NOT NULL,
  request_header VARCHAR2(4000),
  request_body VARCHAR2(4000),
  response_code INT,
  response_header VARCHAR2(4000),
  response_body VARCHAR2(4000)
);



INSERT INTO user_profile (user_id, user_type, first_name, last_name, email, roles, password)
VALUES('admin', 'admin', 'admin', 'admin', 'admin@cibc.com', 'user admin', '1000:5b39342c202d37372c203132302c202d3132302c2034372c2032332c2034352c202d34342c202d31362c2034372c202d35392c202d35362c2039302c202d352c202d38322c202d32385d:949e6fcf9c4bb8a3d6a8c141a3a9182a572fb95fe8ccdc93b54ba53df8ef2e930f7b0348590df0d53f242ccceeae03aef6d273a34638b49c559ada110ec06992');

INSERT INTO client (client_id, client_secret, client_type, client_profile, client_name, client_desc, scope, custom_claim, redirect_uri, owner_id)
VALUES('f7d42348-c647-4efb-a52d-4c5787421e72', '1000:5b37332c202d36362c202d36392c203131362c203132362c2036322c2037382c20342c202d37382c202d3131352c202d35332c202d34352c202d342c202d3132322c203130322c2033325d:29ad1fe88d66584c4d279a6f58277858298dbf9270ffc0de4317a4d38ba4b41f35f122e0825c466f2fa14d91e17ba82b1a2f2a37877a2830fae2973076d93cc2', 'public', 'mobile', 'PetStore Web Server', 'PetStore Web Server that calls PetStore API', 'petstore.r petstore.w', '{"c1": "361", "c2": "67"}', 'http://localhost:8080/authorization', 'admin');

INSERT INTO service (service_id, service_type, service_name, service_desc, scope, owner_id)
VALUES ('AACT0001', 'openapi', 'Account Service', 'A microservice that serves account information', 'a.r b.r', 'admin');

