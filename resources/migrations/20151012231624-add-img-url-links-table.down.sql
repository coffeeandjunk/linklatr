alter table links
  drop image_url varchar(1000),
  drop column modified_title text,
  add column submitted_at time,
  alter column title type text,
  drop column submitted_time timestamp;
