-- Histórico de recebimentos: registros nunca são alterados ou removidos pelo sistema.
-- valor_recebido e desconto abatem o saldo da parcela; juros e multa são acréscimos recebidos além da dívida.

CREATE TABLE bella.recebimentos_conta_receber (
    id               UUID PRIMARY KEY,
    parcela_id       UUID NOT NULL REFERENCES bella.parcelas_conta_receber (id) ON DELETE CASCADE,
    ordem            INTEGER NOT NULL,
    valor_recebido       NUMERIC(12, 2) NOT NULL CHECK (valor_recebido > 0),
    data_recebimento   DATE NOT NULL,
    juros            NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (juros >= 0),
    multa            NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (multa >= 0),
    desconto         NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (desconto >= 0),
    observacao       TEXT,
    criado_em        TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_recebimentos_conta_receber_parcela_id ON bella.recebimentos_conta_receber (parcela_id);
CREATE INDEX idx_recebimentos_conta_receber_data ON bella.recebimentos_conta_receber (data_recebimento);
