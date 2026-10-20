CREATE TABLE bella.parcelas_conta_pagar (
    id               UUID PRIMARY KEY,
    conta_pagar_id   UUID NOT NULL REFERENCES bella.contas_pagar (id) ON DELETE CASCADE,
    ordem            INTEGER NOT NULL,
    numero           INTEGER NOT NULL CHECK (numero > 0),
    valor            NUMERIC(12, 2) NOT NULL CHECK (valor > 0),
    vencimento       DATE NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
                     CHECK (status IN ('PENDENTE', 'PARCIALMENTE_PAGA', 'PAGA', 'CANCELADA'))
);

CREATE INDEX idx_parcelas_conta_pagar_conta_id ON bella.parcelas_conta_pagar (conta_pagar_id);
CREATE INDEX idx_parcelas_conta_pagar_vencimento ON bella.parcelas_conta_pagar (vencimento);
