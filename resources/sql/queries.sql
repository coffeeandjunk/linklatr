-- name: create-user!
-- creates a new user record
INSERT INTO users
(id, first_name, last_name, email, pass)
VALUES (:id, :first_name, :last_name, :email, :pass)

-- name: update-user!
-- update an existing user record
UPDATE users
SET first_name = :first_name, last_name = :last_name, email = :email
WHERE id = :id

-- name: get-user
-- retrieve a user given the id.
SELECT * FROM users
WHERE id = :id

-- name: delete-user!
-- delete a user given the id
DELETE FROM users
WHERE id = :id


-- name: insert-link<!
-- creates a new user record
INSERT INTO links
(url, user_id, title)
VALUES (:url,  :user_id, :title)

-- name: get-links
-- get all links from db
SELECT * from links


-- name: get-url-count
-- get count for the given url
select count(*) from links where url like :url
