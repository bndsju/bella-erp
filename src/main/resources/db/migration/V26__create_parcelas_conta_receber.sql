CREATE TABLE bella.parcelas_conta_receber (
    id               UUID PRIMARY KEY,
    conta_receber_id   UUID NOT NULL REFERENCES bella.contas_receber (id) ON DELETE CASCADE,
    ordem            INTEGER NOT NULL,
    numero           INTEGER NOT NULL CHECK (numero > 0),
    valor            NUMERIC(12, 2) NOT NULL CHECK (valor > 0),
    vencimento       DATE NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
                     CHECK (status IN ('PENDENTE', 'PARCIALMENTE_RECEBIDA', 'RECEBIDA', 'CANCELADA'))
);

CREATE INDEX idx_parcelas_conta_receber_conta_id ON bella.parcelas_conta_receber (conta_receber_id);
CREATE INDEX idx_parcelas_conta_receber_vencimento ON bella.parcelas_conta_receber (vencimento);
