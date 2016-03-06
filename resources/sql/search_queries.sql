-- name: search
-- searches for the given string in the links table
SELECT id, title, link_desc, url, image_url
FROM (SELECT links.id as id,
        links.title as title,
        links.link_desc as link_desc,
        links.url as url,
        links.image_url as image_url,
        to_tsvector('english',coalesce(links.title)) ||
         to_tsvector('english',coalesce(links.url)) || 
        to_tsvector('english',coalesce(links.link_desc)) 
        as document
        FROM links) l_search 
      WHERE l_search.document @@ plainto_tsquery('english',?)
