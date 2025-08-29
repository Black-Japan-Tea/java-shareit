-- Удаление таблиц с каскадом
DROP TABLE IF EXISTS bookings CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS items CASCADE;
DROP TABLE IF EXISTS requests CASCADE;
DROP TABLE IF EXISTS users CASCADE;


-- Создание таблицы пользователей
CREATE TABLE IF NOT EXISTS users
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
) NOT NULL,
    email VARCHAR
(
    255
) NOT NULL UNIQUE
    );

-- Создание таблицы запросов
CREATE TABLE IF NOT EXISTS requests
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    description
    TEXT
    NOT
    NULL,
    requestor_id
    BIGINT
    NOT
    NULL,
    created
    TIMESTAMP
    WITHOUT
    TIME
    ZONE
    DEFAULT
    CURRENT_TIMESTAMP,
    CONSTRAINT
    fk_requestor
    FOREIGN
    KEY
(
    requestor_id
)
    REFERENCES users
(
    id
) ON DELETE CASCADE
    );

-- Создание таблицы предметов
CREATE TABLE IF NOT EXISTS items
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    name
    VARCHAR
(
    255
) NOT NULL,
    description TEXT NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    owner_id BIGINT NOT NULL,
    request_id BIGINT,
    CONSTRAINT fk_owner FOREIGN KEY
(
    owner_id
)
    REFERENCES users
(
    id
) ON DELETE CASCADE,
    CONSTRAINT fk_request FOREIGN KEY
(
    request_id
)
    REFERENCES requests
(
    id
)
  ON DELETE SET NULL
    );

-- Создание таблицы бронирований
CREATE TABLE IF NOT EXISTS bookings
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    start_date
    TIMESTAMP
    WITHOUT
    TIME
    ZONE
    NOT
    NULL,
    end_date
    TIMESTAMP
    WITHOUT
    TIME
    ZONE
    NOT
    NULL,
    item_id
    BIGINT
    NOT
    NULL,
    booker_id
    BIGINT
    NOT
    NULL,
    status
    VARCHAR
(
    20
) NOT NULL,
    CONSTRAINT fk_item FOREIGN KEY
(
    item_id
)
    REFERENCES items
(
    id
) ON DELETE CASCADE,
    CONSTRAINT fk_booker FOREIGN KEY
(
    booker_id
)
    REFERENCES users
(
    id
)
  ON DELETE CASCADE
    );

-- Создание таблицы комментариев
CREATE TABLE IF NOT EXISTS comments
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    text
    TEXT
    NOT
    NULL,
    item_id
    BIGINT
    NOT
    NULL,
    author_id
    BIGINT
    NOT
    NULL,
    created
    TIMESTAMP
    WITHOUT
    TIME
    ZONE,
    FOREIGN
    KEY
(
    item_id
) REFERENCES items
(
    id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    author_id
) REFERENCES users
(
    id
)
  ON DELETE CASCADE

    );



