-- Saldo de estoque por produto. Nunca é escrito diretamente pelo cadastro de Produto:
-- toda alteração passa por uma movimentação registrada em bella.movimentacoes_estoque.

CREATE TABLE bella.saldos_estoque (
    produto_id           UUID PRIMARY KEY REFERENCES bella.produtos (id),
    estoque_fisico        NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (estoque_fisico >= 0),
    quantidade_reservada  NUMERIC(14, 3) NOT NULL DEFAULT 0 CHECK (quantidade_reservada >= 0),
    atualizado_em         TIMESTAMPTZ NOT NULL,

    CHECK (quantidade_reservada <= estoque_fisico)
);
