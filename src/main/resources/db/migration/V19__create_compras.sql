-- Compra de mercadorias junto a fornecedores. Os totais são calculados pelo domínio e guardados
-- aqui como snapshot para leitura. A entrada no estoque só acontece no recebimento (status RECEBIDA).

CREATE TABLE bella.compras (
    id                    UUID PRIMARY KEY,
    fornecedor_id         UUID NOT NULL REFERENCES bella.fornecedores (id),

    data_compra           DATE NOT NULL,
    previsao_recebimento  DATE,
    data_recebimento      DATE,

    desconto_geral        NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (desconto_geral >= 0),
    frete                 NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (frete >= 0),
    outras_despesas       NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (outras_despesas >= 0),

    valor_produtos        NUMERIC(12, 2) NOT NULL DEFAULT 0,
    desconto_total        NUMERIC(12, 2) NOT NULL DEFAULT 0,
    valor_total           NUMERIC(12, 2) NOT NULL DEFAULT 0,

    condicao_pagamento    VARCHAR(100) NOT NULL,
    observacoes           TEXT,

    status                VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO'
                          CHECK (status IN ('RASCUNHO', 'CONFIRMADA', 'RECEBIDA', 'CANCELADA')),
    motivo_cancelamento   TEXT,
    confirmada_em         TIMESTAMPTZ,
    cancelada_em          TIMESTAMPTZ,

    criado_em             TIMESTAMPTZ NOT NULL,
    atualizado_em         TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_compras_fornecedor_id ON bella.compras (fornecedor_id);
CREATE INDEX idx_compras_status ON bella.compras (status);
CREATE INDEX idx_compras_data_compra ON bella.compras (data_compra);
