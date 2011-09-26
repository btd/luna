create table users(
  email VARCHAR (50),
  login VARCHAR (50),
  password VARCHAR (50)
);

create table ssh_keys (
  owner_login VARCHAR (50),
  raw_value VARCHAR (2000) -- is this enough ?
);

create table repositories(
  fs_name VARCHAR(100),
  name VARCHAR(50),
  is_open NUMBER(1),
  owner_login VARCHAR (50)
);

create table push_accesses(
  user_login VARCHAR (50),
  owner_login VARCHAR (50),
  repo_name VARCHAR(50)
);