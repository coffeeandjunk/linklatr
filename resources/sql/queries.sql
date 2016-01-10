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
-- creates a new link record
INSERT INTO links
(url, user_id, title, image_url, link_desc)
VALUES (:url,  :user_id, :title, :image_url, :desc)

-- name: get-links
-- get all links from db
SELECT * from links


-- name: get-url-count
-- get count for the given url
select count(*) from links where url like :url


-- name: get-user-count
-- get count for the users with the same email
SELECT count(*) from users WHERE email LIKE :email


-- name: get-user-id
-- get count for the users with the same email
SELECT id from users WHERE email LIKE :email


-- name: insert-user<!
-- creates a new user record
INSERT INTO users
(first_name, last_name, email, last_login)
VALUES (:firstname, :lastname, :email, current_timestamp)


-- name: get-user-data
-- returns the user data
SELECT * FROM users WHERE id=:id;
