DROP TABLE users CASCADE CONSTRAINTS;
CREATE TABLE users (
  user_id VARCHAR2(32) NOT NULL,
  user_type VARCHAR2(16) NOT NULL,  -- admin, customer, employee, partner
  first_name VARCHAR2(32) NOT NULL,
  last_name VARCHAR2(32) NOT NULL,
  email VARCHAR2(64) NOT NULL,
  password VARCHAR2(1024) NOT NULL,
  create_dt DATE NOT NULL,
  update_dt DATE,
  CONSTRAINT users_pk PRIMARY KEY (user_id)
);

CREATE UNIQUE INDEX email_idx ON users(email);

DROP TABLE clients CASCADE CONSTRAINTS;
CREATE TABLE clients (
  client_id VARCHAR2(36) NOT NULL,
  client_type VARCHAR2(12) NOT NULL,  -- public, confidential, trusted
  client_profile VARCHAR2(10) NOT NULL, -- webserver, mobile, browser, service, batch
  client_secret VARCHAR2(1024) NOT NULL,
  client_name VARCHAR2(32) NOT NULL,
  client_desc VARCHAR2(2048),
  scope VARCHAR2(1024),
  redirect_uri VARCHAR2(1024),
  authenticate_class VARCHAR2(256),
  owner_id VARCHAR2(32) NOT NULL,
  create_dt DATE NOT NULL,
  update_dt DATE,
  CONSTRAINT clients_pk PRIMARY KEY (client_id),
  CONSTRAINT clients_users_fk
    FOREIGN KEY (owner_id)
    REFERENCES users(user_id)
);

DROP TABLE services CASCADE CONSTRAINTS;
CREATE TABLE services (
  service_id VARCHAR2(32) NOT NULL,
  service_type VARCHAR2(8) NOT NULL,  -- api, ms
  service_name VARCHAR2(32) NOT NULL,
  service_desc VARCHAR2(1024),
  scope VARCHAR2(1024),
  owner_id VARCHAR2(32) NOT NULL,
  create_dt DATE NOT NULL,
  update_dt DATE,
  CONSTRAINT services_pk PRIMARY KEY (service_id),
  CONSTRAINT services_users_fk
    FOREIGN KEY (owner_id)
    REFERENCES users(user_id)
);

INSERT INTO users (user_id, user_type, first_name, last_name, email, password, create_dt)
VALUES('admin', 'admin', 'admin', 'admin', 'admin@cibc.com', '1000:5b39342c202d37372c203132302c202d3132302c2034372c2032332c2034352c202d34342c202d31362c2034372c202d35392c202d35362c2039302c202d352c202d38322c202d32385d:949e6fcf9c4bb8a3d6a8c141a3a9182a572fb95fe8ccdc93b54ba53df8ef2e930f7b0348590df0d53f242ccceeae03aef6d273a34638b49c559ada110ec06992', SYSDATE);

INSERT INTO clients (client_id, client_secret, client_type, client_profile, client_name, client_desc, scope, redirect_uri, owner_id, create_dt)
VALUES('f7d42348-c647-4efb-a52d-4c5787421e72', '1000:5b37332c202d36362c202d36392c203131362c203132362c2036322c2037382c20342c202d37382c202d3131352c202d35332c202d34352c202d342c202d3132322c203130322c2033325d:29ad1fe88d66584c4d279a6f58277858298dbf9270ffc0de4317a4d38ba4b41f35f122e0825c466f2fa14d91e17ba82b1a2f2a37877a2830fae2973076d93cc2', 'public', 'mobile', 'PetStore Web Server', 'PetStore Web Server that calls PetStore API', 'petstore.r petstore.w', 'http://localhost:8080/authorization', 'admin', SYSDATE);

INSERT INTO services (service_id, service_type, service_name, service_desc, scope, owner_id, create_dt)
VALUES ('AACT0001', 'ms', 'Account Service', 'A microservice that serves account information', 'a.r b.r', 'admin', SYSDATE);

