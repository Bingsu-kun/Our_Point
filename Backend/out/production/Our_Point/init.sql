CREATE TABLE IF NOT EXISTS user (
    user_id SERIAL,
    user_email VARCHAR(50) NOT NULL,
    user_password VARCHAR(20) NOT NULL,
    user_name VARCHAR(10) NOT NULL,
    user_last_login DATETIME DEFAULT NULL,
    user_created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    CONSTRAINT unq_user_email UNIQUE (user_email),
    CONSTRAINT unq_user_name UNIQUE (user_name)
);

INSERT INTO user(user_id, user_email, user_password, user_name) VALUES (null, 'ADMIN', 'icetime963@gmail.com','')

COMMIT;