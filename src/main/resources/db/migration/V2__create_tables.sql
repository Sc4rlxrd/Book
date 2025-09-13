--- Ativar extensão para gerar UUIDs automaticamente
 CREATE EXTENSION IF NOT EXISTS "pgcrypto";

 -- Tabela de clientes
 CREATE TABLE clients (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     name VARCHAR(100) NOT NULL,
     last_name VARCHAR(100) NOT NULL,
     cpf_number VARCHAR(11) UNIQUE NOT NULL
 );

 -- Tabela de livros
 CREATE TABLE books (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     title VARCHAR(200) NOT NULL,
     author VARCHAR(150),
     isbn VARCHAR(20) UNIQUE,
     client_id UUID NOT NULL,
     CONSTRAINT fk_books_clients FOREIGN KEY (client_id) REFERENCES clients(id)
 );

 -- Tabela de usuários
 CREATE TABLE users (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     email VARCHAR(150) UNIQUE NOT NULL,
     password VARCHAR(255) NOT NULL
 );
