ALTER TABLE genre
    ADD CONSTRAINT uc_genre_name UNIQUE (name);

ALTER TABLE genre
    ADD CONSTRAINT uc_genre_slug UNIQUE (slug);
