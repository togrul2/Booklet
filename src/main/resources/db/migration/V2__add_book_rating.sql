ALTER TABLE book
    ADD rating INTEGER;

ALTER TABLE author
    DROP COLUMN rating;
