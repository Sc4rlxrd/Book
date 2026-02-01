-- 1. Adiciona a coluna active (Soft Delete)
ALTER TABLE clients ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE books ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;


CREATE INDEX idx_clients_cpf ON clients(cpf_number);
CREATE INDEX idx_clients_active ON clients(active);

CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_client_id ON books(client_id);
CREATE INDEX idx_books_active ON books(active);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(active);