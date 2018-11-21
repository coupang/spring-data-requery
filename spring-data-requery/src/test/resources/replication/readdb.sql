CREATE TABLE users (
  id INTEGER AUTO_INCREMENT,
  name varchar(50) NOT NULL,
  PRIMARY KEY(id)
);

insert into users (name) values ('read_1'), ('read_2'), ('read_3'), ('read_4');