CREATE TABLE accounts
(
    id                BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    first_name        VARCHAR(255),
    last_name         VARCHAR(255),
    username          VARCHAR(255)                            NOT NULL,
    password          VARCHAR(255),
    active            BOOLEAN DEFAULT TRUE                    NOT NULL,
    creation_date     TIMESTAMP WITHOUT TIME ZONE,
    modification_date TIMESTAMP WITHOUT TIME ZONE,
    role              VARCHAR(255),
    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uc_accounts_username UNIQUE (username)
);
