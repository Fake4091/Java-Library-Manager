CREATE TABLE people (
  id BIGSERIAL NOT NULL primary key,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL
);

CREATE TABLE books (
  id BIGSERIAL NOT NULL primary key,
  title VARCHAR(255) NOT NULL,
  author VARCHAR(255) NOT NULL,
  borrower BIGINT REFERENCES people(id) DEFAULT NULL
);
