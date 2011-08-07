create table account(
  email VARCHAR (50) not null UNIQUE,
  name VARCHAR (50),
  passwd VARCHAR (50) not null,
  PRIMARY KEY(email)
);

create table ssh_key (
  owner_id VARCHAR (50),
  value VARCHAR (2000), -- is this enough ?
  foreign key (owner_id) references account(email),
  PRIMARY KEY(owner_id)
);