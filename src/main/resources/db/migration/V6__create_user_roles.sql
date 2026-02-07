
CREATE TABLE user_roles (
     user_id UUID NOT NULL,
     roles VARCHAR(255),
     CONSTRAINT fk_user_roles_users FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Indice para performance
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

