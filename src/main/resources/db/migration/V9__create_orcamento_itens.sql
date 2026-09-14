CREATE TABLE bella.orcamento_itens (
    id                UUID PRIMARY KEY,
    orcamento_id      UUID NOT NULL REFERENCES bella.orcamentos (id) ON DELETE CASCADE,
    ordem             INTEGER NOT NULL,
    produto_id        UUID NOT NULL REFERENCES bella.produtos (id),
    quantidade        NUMERIC(12, 3) NOT NULL CHECK (quantidade > 0),
    valor_unitario    NUMERIC(12, 2) NOT NULL CHECK (valor_unitario >= 0),
    subtotal          NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_orcamento_itens_orcamento_id ON bella.orcamento_itens (orcamento_id);
CREATE INDEX idx_orcamento_itens_produto_id ON bella.orcamento_itens (produto_id);
