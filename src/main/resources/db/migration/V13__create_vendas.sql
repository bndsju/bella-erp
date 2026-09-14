-- Uma venda so nasce da conclusao de um pedido; pedido_id e UNIQUE porque
-- cada pedido so pode virar uma venda. subtotal/valor_desconto/valor_total
-- sao um retrato (snapshot) dos valores do pedido no momento da conclusao,
-- nao recalculados depois -- e um registro da operacao, nao um documento vivo.

CREATE TABLE bella.vendas (
    id                  UUID PRIMARY KEY,
    pedido_id           UUID NOT NULL UNIQUE REFERENCES bella.pedidos (id),
    cliente_id          UUID NOT NULL REFERENCES bella.clientes (id),

    percentual_desconto NUMERIC(5, 2) NOT NULL,
    valor_frete         NUMERIC(12, 2) NOT NULL,
    subtotal            NUMERIC(12, 2) NOT NULL,
    valor_desconto      NUMERIC(12, 2) NOT NULL,
    valor_total         NUMERIC(12, 2) NOT NULL,

    forma_pagamento     VARCHAR(20) NOT NULL
                        CHECK (forma_pagamento IN
                               ('DINHEIRO', 'CARTAO_CREDITO', 'CARTAO_DEBITO', 'PIX', 'BOLETO', 'TRANSFERENCIA')),
    condicao_pagamento  VARCHAR(100) NOT NULL,

    status              VARCHAR(10) NOT NULL DEFAULT 'CONCLUIDA' CHECK (status IN ('CONCLUIDA', 'CANCELADA')),

    criado_em           TIMESTAMPTZ NOT NULL,
    atualizado_em       TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_vendas_cliente_id ON bella.vendas (cliente_id);
CREATE INDEX idx_vendas_status ON bella.vendas (status);
