alter table links
  add image_url varchar(1000),
  add column modified_title text,
  drop column submitted_at,
  alter column title type text,
  add column submitted_time timestamp not null default current_timestamp;
