CREATE DATABASE customers;
--DROP DATABASE customers;


--DROP TABLE IF EXISTS customers;
CREATE TABLE IF NOT EXISTS customers(
	id BIGSERIAL PRIMARY KEY,
	first_name VARCHAR (50) NOT NULL,
	last_name VARCHAR (50) NOT NULL,
	birth_date DATE,
	email VARCHAR (50) NOT NULL ,
	deleted boolean DEFAULT FALSE
);