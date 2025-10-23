# 📚 Book Manager API

Uma API pessoal e robusta construída com **Spring Boot**, projetada para o **gerenciamento de clientes e suas coleções de livros**.  
A aplicação segue princípios modernos de **segurança (JWT + Redis)**, **observabilidade (Prometheus/Grafana)** e **resiliência (RabbitMQ + DLQ + Retry)**.

---

## 🚀 Tecnologias

| Categoria | Tecnologia | Descrição |
|------------|-------------|------------|
| **Backend** | Spring Boot & Spring Web | Estrutura principal da API REST. |
| **Persistência Principal** | Spring JPA & PostgreSQL | Armazenamento relacional de clientes e livros. |
| **Mensageria** | RabbitMQ | Comunicação assíncrona entre serviços e filas de retry/DLQ. |
| **Segurança** | Spring Security & JWT | Autenticação e autorização baseada em tokens JWT. |
| **Cache/Token** | Redis | Armazena Refresh Tokens e gerencia blacklist de tokens. |
| **Infraestrutura** | Docker | Containerização dos serviços (API, DB, Redis, RabbitMQ, Mongo). |
| **Migrations** | Flyway | Controle de versão do esquema de banco de dados. |
| **Monitoramento** | Prometheus & Grafana | Coleta e visualização de métricas. |
| **Tratamento de Erros** | ProblemDetail | Respostas padronizadas em conformidade com RFC 7807. |
| **Testes** | JUnit 5 & Mockito | Testes unitários e de integração. |
| **Persistência DLQ** | MongoDB | Armazenamento das mensagens de erro da fila DLQ. |

---

## 💡 Arquitetura Geral

A arquitetura é composta por **dois serviços principais**:

### 🧩 1. API Principal (`Book-Service`)
Responsável pelas operações de autenticação, gerenciamento de clientes e envio de mensagens para RabbitMQ.

**Fluxos:**
- **Síncrono:** autenticação e operações CRUD básicas (GET, DELETE).
- **Assíncrono:** criação de novos clientes via RabbitMQ, com tratamento de retries e DLQ.

### 🧱 2. Serviço DLQ (`Book-DLQ-Service`)
Serviço separado responsável por **consumir mensagens com falha da Dead Letter Queue**, **persisti-las no MongoDB** e **registrar logs detalhados** para auditoria e análise.

---

## 🔒 Endpoints de Autenticação (`/auth`)

| Método | Endpoint | Descrição | Corpo da Requisição | Resposta |
|--------|-----------|------------|---------------------|-----------|
| `POST` | `/auth/register` | Cria um novo usuário. | `{ "email": "...", "password": "..." }` | `200 OK` ou `400 Bad Request` |
| `POST` | `/auth/login` | Realiza login e gera tokens JWT. | `{ "email": "...", "password": "..." }` | `200 OK` (Access + Refresh Tokens) |
| `POST` | `/auth/refresh` | Gera novos tokens usando o refresh token. | `{ "refreshToken": "..." }` | `200 OK` ou `401 Unauthorized` |
| `POST` | `/auth/logout` | Invalida o token atual no Redis. | Header: `Authorization: Bearer <token>` | `200 OK` |

---
## 👤 Endpoints de Clientes (`/v1/clients`)

Todos os endpoints exigem **token de acesso JWT válido**.

Esses endpoints permitem **criar, listar e remover clientes**, juntamente com seus livros associados.  
As operações são **síncronas** e persistem os dados diretamente no **PostgreSQL**.

---

| Método | Endpoint | Descrição | Tipo de Operação |
|--------|-----------|------------|------------------|
| `POST` | `/v1/clients` | Cria um novo cliente e seus livros. | Síncrona |
| `GET` | `/v1/clients` | Lista todos os clientes cadastrados com seus livros. | Síncrona |
| `DELETE` | `/v1/clients/{cpf}` | Remove um cliente com base no CPF informado. | Síncrona |

---

### 🧩 **POST** `/v1/clients`
Cria um cliente e os seus livros associados.

**Corpo da Requisição:**
```json
{
  "name": "João",
  "lastName": "Silva",
  "cpfNumber": "123.456.789-00",
  "books": [
    {
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "isbn": "9780132350884"
    },
    {
      "title": "Effective Java",
      "author": "Joshua Bloch",
      "isbn": "9780134685991"
    }
  ]
}
```

**Resposta – 201 Created:**
```json
{
  "id": 1,
  "name": "João",
  "lastName": "Silva",
  "cpfNumber": "123.456.789-00",
  "books": [
    {
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "isbn": "9780132350884"
    },
    {
      "title": "Effective Java",
      "author": "Joshua Bloch",
      "isbn": "9780134685991"
    }
  ]
}
```

### Códigos de Resposta Comuns

| Código | Motivo |
| :--- | :--- |
| `400 Bad Request` | Dados inválidos (violação de validação) |
| `409 Conflict` | Já existe um cliente com o CPF informado |
---

### 📋 **GET** `/v1/clients`
Retorna todos os clientes cadastrados, incluindo os seus livros associados.

**Resposta – 200 OK:**
```json
[
  {
    "id": 1,
    "name": "João",
    "lastName": "Silva",
    "cpfNumber": "123.456.789-00",
    "books": [
      {
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "isbn": "9780132350884"
      }
    ]
  },
  {
    "id": 2,
    "name": "Maria",
    "lastName": "Souza",
    "cpfNumber": "987.654.321-00",
    "books": []
  }
]
```

---

### 🗑️ **DELETE** `/v1/clients/{cpf}`
Remove um cliente com base no CPF informado.

**Exemplo de Requisição:**
```
DELETE /v1/clients/123.456.789-00
```

**Resposta – 200 OK:**
```json
{}
```

### Códigos de Resposta Comuns

| Código | Motivo |
| :--- | :--- |
| `404 Not Found` | Cliente com CPF informado não encontrado |


## ⚙️ Funcionamento do RabbitMQ

### **1. Filas Configuradas**
- `client.book.queue` → Fila principal de processamento.
- `client.book.queue.retry` → Fila de retry com TTL de 10 segundos.
- `client.book.queue.dlq` → Fila de mensagens inválidas ou que atingiram o limite de retries.

### **2. Validações do Consumer**
O `ClientConsumer` valida e processa mensagens de criação de clientes:
- Validação de campos (`@Valid` + Bean Validation)
- Validação de CPF
- Checagem de duplicidade
- Persistência transacional (Spring TransactionTemplate)
- Envio para DLQ em caso de falhas permanentes
- Reenvio para fila principal (retry) em caso de falhas temporárias

---

## 🛑 Book-DLQ-Service

### 📖 Descrição
O **Book-DLQ-Service** é um microsserviço que consome mensagens enviadas para a **Dead Letter Queue (DLQ)** do `Book-Service`, garantindo que nenhum erro seja perdido.  
Ele persiste as mensagens com falha no **MongoDB**, junto com o motivo da falha e o timestamp, permitindo análise e recuperação posterior.

### ⚙️ Funcionalidades
- **Consumo dedicado** da fila `client.book.queue.dlq`
- **Persistência no MongoDB** das mensagens falhas
- **Registro de logs estruturados (JSON)** com status de processamento
- **Recuperação manual de mensagens** via inspeção no banco

### 🧠 Estrutura de Dados (MongoDB)
Cada mensagem armazenada segue o modelo:

```json
{
  "payload": { ... dados originais do cliente ... },
  "reason": "Motivo da falha (ex: CPF inválido)",
  "receivedAt": "2025-10-22T21:45:00"
}
```
### 🧾 Exemplo de Log Estruturado

```json
{
  "event": "dlq_message",
  "payload": {
    "name": "João",
    "cpf": "000.000.000-00"
  },
  "status": "SUCCESS",
  "reason": "Cliente duplicado",
  "timestamp": "22/10/2025 21:45:00"
}
```
## 📈 Monitoramento e Observabilidade

- **Prometheus:** coleta métricas da API principal (tempo de resposta, status HTTP, etc).
- **Grafana:** exibe dashboards de saúde e consumo das filas.
- **Logs Estruturados:** JSON para melhor integração com ferramentas de log (Elastic, Loki, etc).

---

## 🧪 Testes

- **JUnit 5** para testes unitários.
- **Mockito** para mocks e simulações.
- **Testcontainers** (opcional) pode ser utilizado para testar RabbitMQ, PostgreSQL e Redis em ambiente isolado.

## 📦 Repositórios

| Serviço | Repositório | Descrição |
|----------|--------------|------------|
| **Book-Service** | [Book API](https://github.com/Sc4rlxrd/Book) | API principal para autenticação e gerenciamento de clientes/livros. |
| **Book-DLQ-Service** | [Book DLQ Service](https://github.com/Sc4rlxrd/Book-DLQ-Service) | Serviço de consumo e persistência das mensagens da DLQ. |

---

## 👨‍💻 Autor

**Guilherme dos Santos (Sc4rlxrd)**  
Desenvolvedor Back-End | Java | Spring Boot | Microsserviços  

