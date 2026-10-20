-- Contas a receber: valores a receber, lançados manualmente (ou, futuramente, gerados por Vendas).
-- cliente_id é opcional em lançamentos manuais e obrigatório quando a origem é comercial (regra do domínio).
-- "Vencida" não é status armazenado:
-- é calculada pelo vencimento das parcelas e pelo saldo pendente.
-- origem/origem_referencia_id preparam a integração com Vendas; o índice único parcial garante
-- uma única conta por origem automática.

CREATE TABLE bella.contas_receber (
    id                      UUID PRIMARY KEY,
    descricao               VARCHAR(200) NOT NULL,
    cliente_id              UUID REFERENCES bella.clientes (id),

    valor_total             NUMERIC(12, 2) NOT NULL CHECK (valor_total > 0),
    data_lancamento         DATE NOT NULL,
    observacoes             TEXT,

    origem                  VARCHAR(20) NOT NULL DEFAULT 'MANUAL' CHECK (origem IN ('MANUAL', 'VENDA')),
    origem_referencia_id    UUID,

    status                  VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
                            CHECK (status IN ('PENDENTE', 'PARCIALMENTE_RECEBIDA', 'RECEBIDA', 'CANCELADA')),
    motivo_cancelamento     TEXT,
    cancelada_em            TIMESTAMPTZ,

    criado_em               TIMESTAMPTZ NOT NULL,
    atualizado_em           TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_contas_receber_cliente_id ON bella.contas_receber (cliente_id);
CREATE INDEX idx_contas_receber_status ON bella.contas_receber (status);
CREATE UNIQUE INDEX uq_contas_receber_origem_referencia
    ON bella.contas_receber (origem, origem_referencia_id) WHERE origem_referencia_id IS NOT NULL;
