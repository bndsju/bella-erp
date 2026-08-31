-- orcamento_origem_id guarda a rastreabilidade quando o pedido nasce de um
-- orcamento aprovado; fica nulo em pedidos criados manualmente.

CREATE TABLE bella.pedidos (
    id                   UUID PRIMARY KEY,
    cliente_id           UUID NOT NULL REFERENCES bella.clientes (id),
    orcamento_origem_id  UUID REFERENCES bella.orcamentos (id),
    transportadora_id    UUID REFERENCES bella.transportadoras (id),

    percentual_desconto  NUMERIC(5, 2) NOT NULL DEFAULT 0 CHECK (percentual_desconto BETWEEN 0 AND 100),
    valor_frete          NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (valor_frete >= 0),

    subtotal             NUMERIC(12, 2) NOT NULL DEFAULT 0,
    valor_desconto       NUMERIC(12, 2) NOT NULL DEFAULT 0,
    valor_total          NUMERIC(12, 2) NOT NULL DEFAULT 0,

    condicao_pagamento   VARCHAR(100) NOT NULL,

    status               VARCHAR(20) NOT NULL DEFAULT 'CRIADO'
                          CHECK (status IN
                                 ('CRIADO', 'CONFIRMADO', 'EM_SEPARACAO', 'PRONTO_PARA_ENTREGA', 'ENTREGUE',
                                  'CANCELADO')),

    criado_em            TIMESTAMPTZ NOT NULL,
    atualizado_em        TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_pedidos_cliente_id ON bella.pedidos (cliente_id);
CREATE INDEX idx_pedidos_status ON bella.pedidos (status);
CREATE INDEX idx_pedidos_orcamento_origem_id ON bella.pedidos (orcamento_origem_id);
