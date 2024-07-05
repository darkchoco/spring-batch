-- 종속 테이블을 먼저 삭제하여 두 테이블을 모두 삭제합니다
DROP TABLE IF EXISTS country_capital;
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

CREATE TABLE country_capital
(
    id      INT PRIMARY KEY,
    code    VARCHAR(10),
    capital VARCHAR(100),
    FOREIGN KEY (code) REFERENCES country (code)
);
