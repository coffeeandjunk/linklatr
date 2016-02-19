CREATE TABLE IF NOT EXISTS l2u
(l2uid serial PRIMARY KEY,
  lid int references links(id) NOT NULL,
  uid int references users(id) NOT NULL,
  last_modified timestamp default current_timestamp);

