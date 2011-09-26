create table users(
  email VARCHAR (50) ,
  login VARCHAR (50)  not null UNIQUE,
  password VARCHAR (50) not null
);

create table ssh_keys (
  owner_login VARCHAR (50) not null,
  raw_value VARCHAR (2000) not null, -- is this enough ?
);

create table repositories(
  fs_name VARCHAR(100) not null,
  name VARCHAR2(50) not null,
  is_open NUMBER(1),
  owner_login VARCHAR (50) not null
);