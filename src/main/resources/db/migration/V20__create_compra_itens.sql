CREATE TABLE bella.compra_itens (
    id              UUID PRIMARY KEY,
    compra_id       UUID NOT NULL REFERENCES bella.compras (id) ON DELETE CASCADE,
    ordem           INTEGER NOT NULL,
    produto_id      UUID NOT NULL REFERENCES bella.produtos (id),
    quantidade      NUMERIC(12, 3) NOT NULL CHECK (quantidade > 0),
    custo_unitario  NUMERIC(12, 2) NOT NULL CHECK (custo_unitario >= 0),
    desconto        NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (desconto >= 0),
    valor_total     NUMERIC(12, 2) NOT NULL,

    CONSTRAINT uq_compra_itens_compra_produto UNIQUE (compra_id, produto_id)
);

CREATE INDEX idx_compra_itens_compra_id ON bella.compra_itens (compra_id);
CREATE INDEX idx_compra_itens_produto_id ON bella.compra_itens (produto_id);
