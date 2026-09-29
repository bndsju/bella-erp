-- Histórico de movimentações de estoque. Registro de auditoria: nunca é alterado ou
-- removido após criado. origem_operacao_id guarda o id da operação de origem (ex.: pedido)
-- quando a movimentação foi disparada automaticamente, ficando nulo em movimentações manuais.

CREATE TABLE bella.movimentacoes_estoque (
    id                    UUID PRIMARY KEY,
    produto_id            UUID NOT NULL REFERENCES bella.produtos (id),

    tipo                  VARCHAR(20) NOT NULL
                          CHECK (tipo IN
                                 ('ENTRADA', 'SAIDA', 'AJUSTE_ENTRADA', 'AJUSTE_SAIDA', 'RESERVA',
                                  'LIBERACAO_RESERVA')),
    quantidade            NUMERIC(14, 3) NOT NULL CHECK (quantidade > 0),
    motivo                VARCHAR(200) NOT NULL,
    observacao            TEXT,

    origem                VARCHAR(20) NOT NULL CHECK (origem IN ('MANUAL', 'PEDIDO')),
    origem_operacao_id    UUID,
    usuario_responsavel   VARCHAR(150),

    data_hora             TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_movimentacoes_estoque_produto_id ON bella.movimentacoes_estoque (produto_id);
CREATE INDEX idx_movimentacoes_estoque_tipo ON bella.movimentacoes_estoque (tipo);
CREATE INDEX idx_movimentacoes_estoque_origem_operacao_id ON bella.movimentacoes_estoque (origem_operacao_id);
CREATE INDEX idx_movimentacoes_estoque_data_hora ON bella.movimentacoes_estoque (data_hora);
