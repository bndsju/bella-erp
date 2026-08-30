CREATE TABLE bella.unidades_medida (
    id            UUID PRIMARY KEY,
    nome          VARCHAR(100) NOT NULL,
    sigla         VARCHAR(10) NOT NULL,
    status        VARCHAR(10) NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO', 'INATIVO')),
    criado_em     TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_unidades_medida_status ON bella.unidades_medida (status);
