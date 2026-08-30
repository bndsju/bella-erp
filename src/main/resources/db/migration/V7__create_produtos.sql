CREATE TABLE bella.produtos (
    id                UUID PRIMARY KEY,
    codigo_interno    VARCHAR(50) NOT NULL,
    codigo_barras     VARCHAR(50),
    nome              VARCHAR(150) NOT NULL,
    descricao         TEXT,
    categoria_id      UUID NOT NULL REFERENCES bella.categorias (id),
    unidade_medida_id UUID NOT NULL REFERENCES bella.unidades_medida (id),
    preco_custo       NUMERIC(12, 2) NOT NULL CHECK (preco_custo >= 0),
    preco_venda       NUMERIC(12, 2) NOT NULL CHECK (preco_venda >= 0),
    estoque_minimo    NUMERIC(12, 3) NOT NULL DEFAULT 0 CHECK (estoque_minimo >= 0),
    status            VARCHAR(10) NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO', 'INATIVO')),
    criado_em         TIMESTAMPTZ NOT NULL,
    atualizado_em     TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX idx_produtos_codigo_interno ON bella.produtos (codigo_interno);
CREATE UNIQUE INDEX idx_produtos_codigo_barras ON bella.produtos (codigo_barras);
CREATE INDEX idx_produtos_categoria_id ON bella.produtos (categoria_id);
CREATE INDEX idx_produtos_status ON bella.produtos (status);
