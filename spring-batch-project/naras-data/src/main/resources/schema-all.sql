DROP TABLE IF EXISTS country CASCADE;
CREATE TABLE country
(
    code           VARCHAR(10) PRIMARY KEY,
    common_name    VARCHAR(100),
    official_name  VARCHAR(100),
    flag_emoji     VARCHAR(10),
    flag_img       VARCHAR(255),
    region         VARCHAR(100),
    population     INT,
    google_map_url VARCHAR(255)
);

DROP TABLE IF EXISTS country_capital;
CREATE TABLE country_capital
(
    country_code VARCHAR(10),
    capital      VARCHAR(100),
    PRIMARY KEY (country_code, capital),
    FOREIGN KEY (country_code) REFERENCES country (code)
);

