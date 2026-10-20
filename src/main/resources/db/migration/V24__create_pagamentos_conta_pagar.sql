-- Histórico de pagamentos: registros nunca são alterados ou removidos pelo sistema.
-- valor_pago e desconto abatem o saldo da parcela; juros e multa são acréscimos pagos além da dívida.

CREATE TABLE bella.pagamentos_conta_pagar (
    id               UUID PRIMARY KEY,
    parcela_id       UUID NOT NULL REFERENCES bella.parcelas_conta_pagar (id) ON DELETE CASCADE,
    ordem            INTEGER NOT NULL,
    valor_pago       NUMERIC(12, 2) NOT NULL CHECK (valor_pago > 0),
    data_pagamento   DATE NOT NULL,
    juros            NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (juros >= 0),
    multa            NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (multa >= 0),
    desconto         NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (desconto >= 0),
    observacao       TEXT,
    criado_em        TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_pagamentos_conta_pagar_parcela_id ON bella.pagamentos_conta_pagar (parcela_id);
CREATE INDEX idx_pagamentos_conta_pagar_data ON bella.pagamentos_conta_pagar (data_pagamento);
