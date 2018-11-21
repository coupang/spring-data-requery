CREATE TABLE users (
  id INTEGER AUTO_INCREMENT,
  name varchar(50) NOT NULL,
  PRIMARY KEY(id)
);

insert into users (name) values ('write_1'), ('write_2'), ('write_3'), ('write_4');