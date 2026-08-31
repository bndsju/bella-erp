CREATE TABLE bella.venda_itens (
    id                UUID PRIMARY KEY,
    venda_id          UUID NOT NULL REFERENCES bella.vendas (id) ON DELETE CASCADE,
    ordem             INTEGER NOT NULL,
    produto_id        UUID NOT NULL REFERENCES bella.produtos (id),
    quantidade        NUMERIC(12, 3) NOT NULL CHECK (quantidade > 0),
    valor_unitario    NUMERIC(12, 2) NOT NULL CHECK (valor_unitario >= 0),
    subtotal          NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_venda_itens_venda_id ON bella.venda_itens (venda_id);
CREATE INDEX idx_venda_itens_produto_id ON bella.venda_itens (produto_id);
