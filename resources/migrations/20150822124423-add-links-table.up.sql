CREATE TABLE links
(id serial PRIMARY KEY,
 url varchar,
 user_id int references users(id),
 date_submitted timestamp default current_timestamp,
 date_modified timestamp default current_timestamp,
 tag_ids int[],
 title varchar[300],
 link_desc text,
 submitted_at TIME);
