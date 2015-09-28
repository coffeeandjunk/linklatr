CREATE TABLE users
(id serial PRIMARY KEY,
 first_name VARCHAR(30),
 last_name VARCHAR(30),
 email VARCHAR(30),
 admin BOOLEAN,
 last_login TIME,
 is_active BOOLEAN,
 fav_links int[],
 timestamp timestamp default current_timestamp,
 pass VARCHAR(100));
