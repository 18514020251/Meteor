CREATE TABLE IF NOT EXISTS screening (
    id                BIGINT PRIMARY KEY,
    available_tickets INT NOT NULL,
    sold_tickets      INT NOT NULL,
    version           INT NOT NULL DEFAULT 0,
    update_time       DATETIME NOT NULL,
    deleted           TINYINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;