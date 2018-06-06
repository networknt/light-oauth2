DROP table IF EXISTS  user_profile;
DROP table IF EXISTS  client;
DROP table IF EXISTS  service;
DROP table IF EXISTS  service_endpoint;
DROP table IF EXISTS  client_service;
DROP table IF EXISTS  refresh_token;
DROP table IF EXISTS  audit_log;

create table user_profile (
  user_id varchar PRIMARY KEY,
  user_type varchar,  -- admin, customer, employee, partner
  first_name varchar,
  last_name varchar,
  email varchar,
  password varchar
);

CREATE UNIQUE INDEX email_idx ON user_profile(email);

create table client (
  client_id VARCHAR PRIMARY KEY,
  client_secret VARCHAR,
  client_type VARCHAR,  -- public, confidential, trusted
  client_profile VARCHAR, -- server, mobile, service, batch, browser
  client_name VARCHAR,
  client_desc VARCHAR,
  scope VARCHAR,
  custom_claim VARCHAR,   -- custom claim(s) in json format that will be included in the jwt token
  redirect_uri VARCHAR,
  authenticate_class VARCHAR,
  owner_id VARCHAR
);

create table service (
  service_id VARCHAR PRIMARY KEY,
  service_type VARCHAR,  -- swagger, openapi, graphql, hybrid
  service_name VARCHAR,
  service_desc VARCHAR,
  scope VARCHAR,
  owner_id VARCHAR
);

create table service_endpoint (
  service_id VARCHAR,
  endpoint VARCHAR,  -- different framework will have different endpoint format.
  operation VARCHAR,
  scope VARCHAR,
  PRIMARY KEY (service_id, endpoint),
  FOREIGN KEY (service_id) REFERENCES service(service_id)
);

create table client_service (
  client_id VARCHAR NOT NULL,
  service_id VARCHAR NOT NULL,
  endpoint VARCHAR NOT NULL,  -- different framework will have different endpoint format.
  PRIMARY KEY (client_id, service_id, endpoint),
  FOREIGN KEY (service_id, endpoint) REFERENCES service_endpoint(service_id, endpoint),
  FOREIGN KEY (client_id) REFERENCES client(client_id)
);

CREATE TABLE refresh_token (
  user_id VARCHAR2(36) NOT NULL,
  client_id VARCHAR2(36) NOT NULL,
  scope VARCHAR2(64) NOT NULL,
  refresh_token VARCHAR2(256) NOT NULL,
  PRIMARY KEY (user_id, client_id, refresh_token),
  FOREIGN KEY (user_id) REFERENCES user_profile(user_id),
  FOREIGN KEY (client_id) REFERENCES client(client_id)
);

create table audit_log (
  log_id BIGINT, -- system milliseonds from 1970.
  service_id VARCHAR(32) NOT NULL,
  endpoint VARCHAR(256) NOT NULL,
  request_header VARCHAR(4096),
  request_body VARCHAR(4096),
  response_code INT,
  response_header VARCHAR(4096),
  response_body VARCHAR(4096)
);


INSERT INTO user_profile(user_id, user_type, first_name, last_name, email, password) VALUES('admin', 'admin', 'admin', 'admin', 'admin@networknt.com', '1000:5b39342c202d37372c203132302c202d3132302c2034372c2032332c2034352c202d34342c202d31362c2034372c202d35392c202d35362c2039302c202d352c202d38322c202d32385d:949e6fcf9c4bb8a3d6a8c141a3a9182a572fb95fe8ccdc93b54ba53df8ef2e930f7b0348590df0d53f242ccceeae03aef6d273a34638b49c559ada110ec06992');
INSERT INTO user_profile(user_id, user_type, first_name, last_name, email, password) VALUES('admin2', 'admin2', 'admin2', 'admin2', 'admin2@networknt.com', '1000:5b39342c202d37372c203132302c202d3132302c2034372c2032332c2034352c202d34342c202d31362c2034372c202d35392c202d35362c2039302c202d352c202d38322c202d32385d:949e6fcf9c4bb8a3d6a8c141a3a9182a572fb95fe8ccdc93b54ba53df8ef2e930f7b0348590df0d53f242ccceeae03aef6d273a34638b49c559ada110ec06992');

INSERT INTO client (client_id, client_type, client_secret, client_profile, client_name, client_desc, scope, custom_claim, redirect_uri, authenticate_class, owner_id) VALUES('f7d42348-c647-4efb-a52d-4c5787421e72', 'public', '1000:5b37332c202d36362c202d36392c203131362c203132362c2036322c2037382c20342c202d37382c202d3131352c202d35332c202d34352c202d342c202d3132322c203130322c2033325d:29ad1fe88d66584c4d279a6f58277858298dbf9270ffc0de4317a4d38ba4b41f35f122e0825c466f2fa14d91e17ba82b1a2f2a37877a2830fae2973076d93cc2', 'mobile', 'PetStore Web Server', 'PetStore Web Server that calls PetStore API', 'petstore.r petstore.w', '{"c1": "361", "c2": "67"}', 'http://localhost:8080/authorization', null, 'admin' );
INSERT INTO client (client_id, client_type, client_secret, client_profile, client_name, client_desc, scope, custom_claim, redirect_uri, authenticate_class, owner_id) VALUES('59f347a0-c92d-11e6-9d9d-cec0c932ce01', 'public', '1000:5b37332c202d36362c202d36392c203131362c203132362c2036322c2037382c20342c202d37382c202d3131352c202d35332c202d34352c202d342c202d3132322c203130322c2033325d:29ad1fe88d66584c4d279a6f58277858298dbf9270ffc0de4317a4d38ba4b41f35f122e0825c466f2fa14d91e17ba82b1a2f2a37877a2830fae2973076d93cc2', 'mobile', 'PetStore Web Server', 'PetStore Web Server that calls PetStore API', 'petstore.r petstore.w', '{"c1": "361", "c2": "67"}', 'http://localhost:8080/authorization', null, 'admin' );

INSERT INTO service (service_id, service_type, service_name, service_desc, scope, owner_id) VALUES ('AACT0001', 'openapi', 'Account Service', 'A microservice that serves account information', 'a.r b.r', 'admin');

INSERT INTO service_endpoint(service_id, endpoint, operation, scope) VALUES ('AACT0001', '/v1/data@get', 'retrieveData', 'data.r');