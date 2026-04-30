CREATE TABLE IF NOT EXISTS blocks (
    block_index INTEGER PRIMARY KEY,
    username TEXT NOT NULL,
    salt TEXT NOT NULL,
    auth_salt TEXT NOT NULL,
    password_hash TEXT NOT NULL,
    public_key TEXT,
    encrypted_private_key TEXT,
    signature TEXT,
    previous_hash TEXT NOT NULL,
    current_hash TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    service_passwords TEXT
);

CREATE TABLE IF NOT EXISTS logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL,
    action TEXT NOT NULL,
    timestamp INTEGER NOT NULL
);

