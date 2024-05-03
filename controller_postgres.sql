CREATE DATABASE controllerdb
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LOCALE_PROVIDER = 'libc'
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

CREATE TABLE Users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    birth_date DATE NOT NULL,
    address VARCHAR(255),
    phone_number VARCHAR(20),
    CONSTRAINT email_unique UNIQUE (email),
    CONSTRAINT valid_email CHECK (email ~ '[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,4}$'),
    CONSTRAINT valid_birth_date CHECK (birth_date < CURRENT_DATE)
);

