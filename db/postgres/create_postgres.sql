DROP TABLE IF EXISTS user_profile;
CREATE TABLE user_profile (
  user_id VARCHAR(32) NOT NULL,
  user_type VARCHAR(16) NOT NULL,  -- admin, customer, employee, partner
  first_name VARCHAR(32) NOT NULL,
  last_name VARCHAR(32) NOT NULL,
  email VARCHAR(64) NOT NULL,
  password VARCHAR(1024) NOT NULL,
  PRIMARY KEY (user_id)
);

CREATE UNIQUE INDEX email_idx ON user_profile(email);

DROP TABLE IF EXISTS client;
CREATE TABLE client (
  client_id VARCHAR(36) NOT NULL,
  client_type VARCHAR(12) NOT NULL,  -- public, confidential, trusted
  client_profile VARCHAR(10) NOT NULL, -- webserver, mobile, browser, service, batch
  client_secret VARCHAR(1024) NOT NULL,
  client_name VARCHAR(32) NOT NULL,
  client_desc VARCHAR(2048),
  scope VARCHAR(1024),
  custom_claim VARCHAR(4000), -- custom claim(s) in json format that will be included in the jwt token
  redirect_uri VARCHAR(1024),
  authenticate_class VARCHAR(256),
  owner_id VARCHAR(32) NOT NULL,
  PRIMARY KEY (client_id),
  FOREIGN KEY (owner_id) REFERENCES user_profile(user_id)
);

DROP TABLE IF EXISTS service;
CREATE TABLE service (
  service_id VARCHAR(32) NOT NULL,
  service_type VARCHAR(16) NOT NULL,  -- swagger, openapi, graphql, hybrid
  service_name VARCHAR(32) NOT NULL,
  service_desc VARCHAR(1024),
  scope VARCHAR(1024),
  owner_id VARCHAR(32) NOT NULL,
  PRIMARY KEY (service_id),
  FOREIGN KEY (owner_id) REFERENCES user_profile(user_id)
);

DROP TABLE IF EXISTS service_endpoint;
CREATE TABLE service_endpoint (
  service_id VARCHAR(32) NOT NULL,
  endpoint VARCHAR(256) NOT NULL,  -- different framework will have different endpoint format.
  operation VARCHAR(256) NOT NULL,
  scope VARCHAR(64) NOT NULL,
  PRIMARY KEY (service_id, endpoint),
  FOREIGN KEY (service_id) REFERENCES service(service_id)
);


DROP TABLE IF EXISTS client_service;
CREATE TABLE client_service (
  client_id VARCHAR(36) NOT NULL,
  service_id VARCHAR(32) NOT NULL,
  endpoint VARCHAR(256) NOT NULL,  -- different framework will have different endpoint format.
  PRIMARY KEY (client_id, service_id, endpoint),
  FOREIGN KEY (service_id, endpoint) REFERENCES service_endpoint(service_id, endpoint),
  FOREIGN KEY (client_id) REFERENCES client(client_id)
);

DROP TABLE IF EXISTS audit_log;
create table audit_log (
  log_id INT, -- system milliseonds from 1970.
  service_id VARCHAR(32) NOT NULL,
  endpoint VARCHAR(256) NOT NULL,
  request_header VARCHAR(4096),
  request_body VARCHAR(4096),
  response_code INT,
  response_header VARCHAR(4096),
  response_body VARCHAR(4096)
);


INSERT INTO user_profile (user_id, user_type, first_name, last_name, email, password)
VALUES('admin', 'admin', 'admin', 'admin', 'admin@cibc.com', '1000:5b39342c202d37372c203132302c202d3132302c2034372c2032332c2034352c202d34342c202d31362c2034372c202d35392c202d35362c2039302c202d352c202d38322c202d32385d:949e6fcf9c4bb8a3d6a8c141a3a9182a572fb95fe8ccdc93b54ba53df8ef2e930f7b0348590df0d53f242ccceeae03aef6d273a34638b49c559ada110ec06992');

INSERT INTO client (client_id, client_secret, client_type, client_profile, client_name, client_desc, scope, custom_claim, redirect_uri, owner_id)
VALUES('f7d42348-c647-4efb-a52d-4c5787421e72', '1000:5b37332c202d36362c202d36392c203131362c203132362c2036322c2037382c20342c202d37382c202d3131352c202d35332c202d34352c202d342c202d3132322c203130322c2033325d:29ad1fe88d66584c4d279a6f58277858298dbf9270ffc0de4317a4d38ba4b41f35f122e0825c466f2fa14d91e17ba82b1a2f2a37877a2830fae2973076d93cc2', 'public', 'mobile', 'PetStore Web Server', 'PetStore Web Server that calls PetStore API', 'petstore.r petstore.w', '{"c1": "361", "c2": "67"}', 'http://localhost:8080/authorization', 'admin');

INSERT INTO service (service_id, service_type, service_name, service_desc, scope, owner_id)
VALUES ('AACT0001', 'openapi', 'Account Service', 'A microservice that serves account information', 'a.r b.r', 'admin');

