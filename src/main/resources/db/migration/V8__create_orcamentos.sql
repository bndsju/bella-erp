-- Cabecalho do orcamento. subtotal/valor_desconto/valor_total sao denormalizados
-- (recalculados pelo dominio a cada cadastro/edicao) para nao exigir agregacao
-- dos itens sempre que a listagem for consultada.

CREATE TABLE bella.orcamentos (
    id                   UUID PRIMARY KEY,
    cliente_id           UUID NOT NULL REFERENCES bella.clientes (id),

    percentual_desconto  NUMERIC(5, 2) NOT NULL DEFAULT 0 CHECK (percentual_desconto BETWEEN 0 AND 100),
    valor_frete          NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (valor_frete >= 0),

    subtotal             NUMERIC(12, 2) NOT NULL DEFAULT 0,
    valor_desconto       NUMERIC(12, 2) NOT NULL DEFAULT 0,
    valor_total          NUMERIC(12, 2) NOT NULL DEFAULT 0,

    prazo_validade       DATE NOT NULL,
    condicao_pagamento   VARCHAR(100) NOT NULL,
    observacoes          TEXT,

    status               VARCHAR(10) NOT NULL DEFAULT 'RASCUNHO'
                          CHECK (status IN ('RASCUNHO', 'ENVIADO', 'APROVADO', 'RECUSADO', 'EXPIRADO', 'CANCELADO')),

    criado_em            TIMESTAMPTZ NOT NULL,
    atualizado_em        TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_orcamentos_cliente_id ON bella.orcamentos (cliente_id);
CREATE INDEX idx_orcamentos_status ON bella.orcamentos (status);
