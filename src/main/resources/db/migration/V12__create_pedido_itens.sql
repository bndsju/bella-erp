CREATE TABLE bella.pedido_itens (
    id                UUID PRIMARY KEY,
    pedido_id         UUID NOT NULL REFERENCES bella.pedidos (id) ON DELETE CASCADE,
    ordem             INTEGER NOT NULL,
    produto_id        UUID NOT NULL REFERENCES bella.produtos (id),
    quantidade        NUMERIC(12, 3) NOT NULL CHECK (quantidade > 0),
    valor_unitario    NUMERIC(12, 2) NOT NULL CHECK (valor_unitario >= 0),
    subtotal          NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_pedido_itens_pedido_id ON bella.pedido_itens (pedido_id);
CREATE INDEX idx_pedido_itens_produto_id ON bella.pedido_itens (produto_id);
