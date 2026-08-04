# Bella — Backend

Plataforma web para gestão de pequenos negócios. Projeto de extensão (ADS).

## Stack

- Java 21 + Spring Boot 3
- Arquitetura Hexagonal (Ports & Adapters)
- PostgreSQL + Flyway (migrations versionadas)
- Docker / Docker Compose
- JUnit 5 + Mockito + Testcontainers
- CI: GitHub Actions

## Arquitetura

```
com.bella.backend
├── domain/              # Regras de negócio puras (sem dependência de framework)
│   └── <feature>/
│       ├── model/        # Entidades e Value Objects
│       └── port/
│           ├── in/       # Casos de uso (contratos)
│           └── out/      # Contratos de persistência/integração
│
├── application/         # Implementação dos casos de uso
│   └── <feature>/
│       └── usecase/
│
├── adapter/
│   ├── in/web/           # Controllers REST, DTOs
│   └── out/persistence/  # Repositórios JPA, entidades de persistência
│
└── config/               # Configurações do Spring (CORS, beans, etc.)
```

**Regra principal:** o domínio nunca depende de Spring/JPA/HTTP. As dependências
sempre apontam para dentro (adapters → application → domain).

## Como rodar localmente

### Opção 1 — Docker Compose (recomendado)

```bash
docker compose up --build
```

Sobe o backend (porta `8080`) e o PostgreSQL (porta `5432`) juntos.

### Opção 2 — Backend local + Postgres via Docker

```bash
docker compose up postgres -d
mvn spring-boot:run
```

## Rodando os testes

```bash
mvn test
```

Os testes de integração usam Testcontainers, então é necessário ter o Docker
rodando localmente mesmo para rodar os testes.

## Migrations (Flyway)

Novas migrations vão em `src/main/resources/db/migration`, seguindo o padrão:

```
V{numero}__descricao_curta.sql
```

Exemplo: `V2__create_clientes.sql`

Nunca altere uma migration já aplicada/commitada — crie uma nova.

## Fluxo de trabalho (Git)

- 1 branch por estória: `feature/nome-da-estoria`
- Commits pequenos e descritivos
- PR para `main` ao concluir a estória (ou merge direto, se for só você)
- CI roda automaticamente a cada push/PR (compila + testa)
