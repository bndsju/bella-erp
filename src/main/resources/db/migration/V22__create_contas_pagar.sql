-- Contas a pagar: obrigações financeiras lançadas manualmente (ou, futuramente, geradas por Compras).
-- fornecedor_id é opcional (energia, aluguel, internet etc.). "Vencida" não é status armazenado:
-- é calculada pelo vencimento das parcelas e pelo saldo pendente.
-- origem/origem_referencia_id preparam a integração com Compras; o índice único parcial garante
-- uma única conta por origem automática.

CREATE TABLE bella.contas_pagar (
    id                      UUID PRIMARY KEY,
    descricao               VARCHAR(200) NOT NULL,
    fornecedor_id           UUID REFERENCES bella.fornecedores (id),
    categoria_despesa_id    UUID NOT NULL REFERENCES bella.categorias_despesa (id),

    valor_total             NUMERIC(12, 2) NOT NULL CHECK (valor_total > 0),
    data_lancamento         DATE NOT NULL,
    observacoes             TEXT,

    origem                  VARCHAR(20) NOT NULL DEFAULT 'MANUAL' CHECK (origem IN ('MANUAL', 'COMPRA')),
    origem_referencia_id    UUID,

    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
                            CHECK (status IN ('PENDENTE', 'PARCIALMENTE_PAGA', 'PAGA', 'CANCELADA')),
    motivo_cancelamento     TEXT,
    cancelada_em            TIMESTAMPTZ,

    criado_em               TIMESTAMPTZ NOT NULL,
    atualizado_em           TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_contas_pagar_fornecedor_id ON bella.contas_pagar (fornecedor_id);
CREATE INDEX idx_contas_pagar_categoria_despesa_id ON bella.contas_pagar (categoria_despesa_id);
CREATE INDEX idx_contas_pagar_status ON bella.contas_pagar (status);
CREATE UNIQUE INDEX uq_contas_pagar_origem_referencia
    ON bella.contas_pagar (origem, origem_referencia_id) WHERE origem_referencia_id IS NOT NULL;
