CREATE TABLE IF NOT EXISTS post(
id serial PRIMARY KEY,
names text,
textVcancy text,
link text UNIQUE ,
created timestamp
);