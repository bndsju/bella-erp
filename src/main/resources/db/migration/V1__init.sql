-- Migration inicial do projeto Bella.
-- Serve apenas para validar que o Flyway está configurado corretamente.
-- As tabelas de domínio (clientes, produtos, etc.) virão nas próximas migrations,
-- uma por estória (ex: V2__create_clientes.sql, V3__create_produtos.sql).

CREATE SCHEMA IF NOT EXISTS bella;
