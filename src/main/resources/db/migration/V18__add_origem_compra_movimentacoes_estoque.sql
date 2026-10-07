-- Compras passa a gerar entradas de estoque no recebimento; a origem COMPRA precisa ser aceita.

ALTER TABLE bella.movimentacoes_estoque DROP CONSTRAINT IF EXISTS movimentacoes_estoque_origem_check;
ALTER TABLE bella.movimentacoes_estoque
    ADD CONSTRAINT movimentacoes_estoque_origem_check CHECK (origem IN ('MANUAL', 'PEDIDO', 'COMPRA'));
