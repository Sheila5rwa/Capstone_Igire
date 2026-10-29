CREATE TABLE customers (
                           id            SERIAL PRIMARY KEY,
                           full_name     VARCHAR(100) NOT NULL,
                           email         VARCHAR(100) NOT NULL UNIQUE,
                           phone_number  VARCHAR(20)  NOT NULL,
                           pin_hash      VARCHAR(64)  NOT NULL
);

CREATE TABLE accounts (
                          id            SERIAL PRIMARY KEY,
                          customer_id   INT NOT NULL REFERENCES customers(id),
                          account_type  VARCHAR(20) NOT NULL CHECK (account_type IN ('WALLET','SAVINGS')),
                          balance       NUMERIC(15,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
                          status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                          created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
                              id               SERIAL PRIMARY KEY,
                              account_id       INT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
                              reference_id     VARCHAR(50) NOT NULL,
                              transaction_type VARCHAR(20) NOT NULL,
                              amount           NUMERIC(15,2) NOT NULL CHECK (amount > 0),
                              created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE processed_requests (
                                    id            SERIAL PRIMARY KEY,
                                    reference_id  VARCHAR(50) NOT NULL UNIQUE,
                                    processed_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);