CREATE TABLE bella.categorias (
    id            UUID PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL,
    status        VARCHAR(10) NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO', 'INATIVO')),
    criado_em     TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_categorias_status ON bella.categorias (status);
