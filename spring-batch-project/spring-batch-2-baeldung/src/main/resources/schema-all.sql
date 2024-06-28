-- DROP TABLE coffee IF EXISTS;
DROP TABLE IF EXISTS coffee;

CREATE TABLE coffee
(
    coffee_id       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    brand           VARCHAR(20),
    origin          VARCHAR(20),
    characteristics VARCHAR(30)
);

-- DROP TABLE medicine IF EXISTS;
DROP TABLE IF EXISTS medicine;

CREATE TABLE medicine
(
    med_id          VARCHAR(36) PRIMARY KEY,
    name            VARCHAR(30),
    type            VARCHAR(30),
    expiration_date TIMESTAMP,
    original_price  DECIMAL,
    sale_price      DECIMAL
);
