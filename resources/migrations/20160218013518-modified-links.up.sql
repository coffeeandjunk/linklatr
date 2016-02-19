CREATE TABLE  IF NOT EXISTS modified_links_data
(mlink_id serial PRIMARY KEY,
  user_id int references users(id),
  link_id int references links(id),
  title varchar[300],
  link_desc text,
  last_modified timestamp default current_timestamp);
