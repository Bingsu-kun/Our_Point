CREATE TABLE IF NOT EXISTS user (
    user_id SERIAL,
    user_email VARCHAR(50) NOT NULL,
    user_password VARCHAR(100) NOT NULL,
    user_name VARCHAR(20) NOT NULL,
    user_last_login DATETIME DEFAULT NULL,
    user_created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    CONSTRAINT unq_user_email UNIQUE (user_email),
    CONSTRAINT unq_user_name UNIQUE (user_name)
);

CREATE TABLE IF NOT EXISTS role (
    role_no INT NOT NULL PRIMARY KEY,
    role_name VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id REFERENCES user (user_id) ON DELETE RESTRICT,
    role_no REFERENCES role (role_no) ON DELETE RESTRICT
);


INSERT INTO role (role_no, role_name) VALUES ('0','ADMIN'), ('1','USER');

COMMIT;