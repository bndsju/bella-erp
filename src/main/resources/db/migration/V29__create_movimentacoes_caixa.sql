-- Fluxo de caixa: cada operação financeira é um único registro que passa de PREVISTO para REALIZADO
-- (nunca dois registros), evitando somar o previsto ao realizado. Movimentações automáticas
-- (CONTA_RECEBER, CONTA_PAGAR) carregam a referência do registro de origem; o índice único parcial
-- impede mais de uma movimentação por origem/referência. Realizadas nunca são apagadas:
-- cancelamento/estorno só marca o registro como cancelado.

CREATE TABLE bella.movimentacoes_caixa (
    id                    UUID PRIMARY KEY,
    tipo                  VARCHAR(10) NOT NULL CHECK (tipo IN ('ENTRADA', 'SAIDA')),
    descricao             VARCHAR(200) NOT NULL,
    categoria_id          UUID REFERENCES bella.categorias_fluxo_caixa (id),
    valor                 NUMERIC(12, 2) NOT NULL CHECK (valor > 0),

    situacao              VARCHAR(10) NOT NULL CHECK (situacao IN ('PREVISTO', 'REALIZADO')),
    data_prevista         DATE NOT NULL,
    data_efetiva          DATE,

    origem                VARCHAR(20) NOT NULL
                          CHECK (origem IN ('CONTA_RECEBER', 'CONTA_PAGAR', 'LANCAMENTO_MANUAL')),
    origem_referencia_id  UUID,
    observacoes           TEXT,

    cancelada             BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_cancelamento   TEXT,
    cancelada_em          TIMESTAMPTZ,

    criado_em             TIMESTAMPTZ NOT NULL,
    atualizado_em         TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_movimentacoes_caixa_data_efetiva
        CHECK ((situacao = 'REALIZADO' AND data_efetiva IS NOT NULL)
            OR (situacao = 'PREVISTO' AND data_efetiva IS NULL)),
    CONSTRAINT ck_movimentacoes_caixa_referencia
        CHECK ((origem = 'LANCAMENTO_MANUAL' AND origem_referencia_id IS NULL)
            OR (origem <> 'LANCAMENTO_MANUAL' AND origem_referencia_id IS NOT NULL))
);

CREATE INDEX idx_movimentacoes_caixa_tipo ON bella.movimentacoes_caixa (tipo);
CREATE INDEX idx_movimentacoes_caixa_categoria_id ON bella.movimentacoes_caixa (categoria_id);
CREATE INDEX idx_movimentacoes_caixa_situacao ON bella.movimentacoes_caixa (situacao);
CREATE INDEX idx_movimentacoes_caixa_data_prevista ON bella.movimentacoes_caixa (data_prevista);
CREATE INDEX idx_movimentacoes_caixa_data_efetiva ON bella.movimentacoes_caixa (data_efetiva);
CREATE UNIQUE INDEX uq_movimentacoes_caixa_origem_referencia
    ON bella.movimentacoes_caixa (origem, origem_referencia_id) WHERE origem_referencia_id IS NOT NULL;
