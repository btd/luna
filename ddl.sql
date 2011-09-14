create table users(
  email VARCHAR (50) ,
  login VARCHAR (50)  not null UNIQUE,
  password VARCHAR (50) not null,
  PRIMARY KEY(login)
);

create table ssh_keys (
  owner_login VARCHAR (50) not null,
  raw_value VARCHAR (2000) not null, -- is this enough ?
  foreign key (owner_login) references users(login),
);

create table repositories(
  fs_name VARCHAR(100) not null,
  name VARCHAR2(50) not null,
  owner_login VARCHAR (50) not null,
   foreign key (owner_login) references users(login),
);