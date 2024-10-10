ALTER TABLE book
    DROP COLUMN rating;

ALTER TABLE author
    DROP COLUMN birth_date;

ALTER TABLE author
    DROP COLUMN death_date;

ALTER TABLE author
    ADD birth_date date NOT NULL DEFAULT now();

ALTER TABLE author
    ALTER COLUMN birth_date DROP DEFAULT;

ALTER TABLE author
    ADD death_date date;