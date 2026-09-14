CREATE TABLE bella.transportadoras (
    id            UUID PRIMARY KEY,
    nome          VARCHAR(150) NOT NULL,
    telefone      VARCHAR(20),
    status        VARCHAR(10) NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO', 'INATIVO')),
    criado_em     TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_transportadoras_status ON bella.transportadoras (status);
