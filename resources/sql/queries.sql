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
(url, title, image_url, link_desc)
VALUES (:url, :title, :image_url, :desc)

-- name: get-user-link-count
-- get count of all the links from db for the given user-id
SELECT  count(*) 
FROM l2u WHERE l2u.uid = :user_id


-- name: get-links
-- get all links from db for the given id
SELECT  links.id, links.url, links.title, links.link_desc, links.image_url, l2u.uid 
FROM links INNER JOIN l2u ON 
l2u.lid = links.id AND l2u.uid = :user_id
LIMIT :limit OFFSET :offset

--name: get-link-details
-- fetches all details for a given link-id
SELECT id, url, image_url, title, link_desc
FROM links WHERE id = :lid


-- name: get-url-mapping-count 
-- gets the count for the given link-id and the user-id
SELECT count(*) from l2u WHERE 
Lid = :lid AND Uid = :uid

-- name: link-map-count
-- gets the count for the given link-id and the user-id
SELECT count(*) from l2u WHERE 
Lid = :lid 

-- name: get-url-count
-- get count for the given url
select id from links where url like :url

-- name: insert-link2user<!
-- inserts in the l2u table for a new link
INSERT INTO l2u
(lid, uid) values(:lid, :uid)


-- name: insert-user2link<!
-- inserts in the l2u table for a new link
INSERT INTO l2u
(lid, uid) values(:lid, :uid)


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
SELECT * FROM users WHERE id = :id


-- name: delete-mapping-for-link!
-- deletes the row in l2u table for given link_id and user_id
DELETE from l2u
WHERE lid = :lid AND uid = :uid

-- name: delete-link!
-- deletes the link from the links table
DELETE from links
WHERE id = :lid 
