-- Tabela base de clientes: dados comuns a Pessoa Física e Pessoa Jurídica,
-- mais status (soft delete) e timestamps. Os dados específicos de cada tipo
-- ficam em clientes_pf / clientes_pj (ver V3 e V4).

CREATE TABLE bella.clientes (
    id              UUID PRIMARY KEY,
    tipo_pessoa     VARCHAR(2) NOT NULL CHECK (tipo_pessoa IN ('PF', 'PJ')),

    telefone        VARCHAR(20),
    celular         VARCHAR(20) NOT NULL,
    email           VARCHAR(150) NOT NULL,

    cep             VARCHAR(9),
    logradouro      VARCHAR(150),
    numero          VARCHAR(20),
    complemento     VARCHAR(100),
    bairro          VARCHAR(100),
    cidade          VARCHAR(100),
    uf              VARCHAR(2),

    observacoes     TEXT,

    status          VARCHAR(10) NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO', 'INATIVO')),

    criado_em       TIMESTAMPTZ NOT NULL,
    atualizado_em   TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_clientes_status ON bella.clientes (status);
