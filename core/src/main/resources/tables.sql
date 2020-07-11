CREATE TABLE listings
(
    uuid        UUID PRIMARY KEY,
    title       VARCHAR UNIQUE NOT NULL,
    address     VARCHAR UNIQUE NOT NULL,
    price       NUMERIC        NOT NULL
        CONSTRAINT positive_price CHECK ( price > 0 ),
    description VARCHAR        NOT NULL,
    date_posted TIMESTAMP      NOT NULL,
    url         VARCHAR        NOT NULL
);

CREATE TABLE users
(
    uuid     UUID PRIMARY KEY,
    name     VARCHAR UNIQUE NOT NULL,
    password VARCHAR        NOT NULL
);

CREATE TABLE watched
(
    user_id    UUID NOT NULL,
    listing_id UUID NOT NULL,
    CONSTRAINT user_id_fkey FOREIGN KEY (user_id)
        REFERENCES users (uuid) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE NO ACTION,
    CONSTRAINT listing_id_fkey FOREIGN KEY (listing_id)
        REFERENCES listings (uuid) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE NO ACTION
);