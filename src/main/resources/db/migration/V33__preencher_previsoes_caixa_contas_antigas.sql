-- Contas a pagar e a receber criadas antes da integração com o fluxo de caixa não têm previsão lá.
-- Cria, para cada parcela com saldo pendente de conta não cancelada, a previsão que o sistema criaria
-- hoje. É idempotente: parcelas que já têm a previsão vigente são ignoradas.
--
-- A referência da previsão é o id da parcela enquanto não houve pagamento; depois, deriva do id da
-- parcela e do número de pagamentos, exatamente como ReferenciaProjecaoParcela (UUID de nome, versão 3,
-- do MD5 de "<id da parcela>#<pagamentos>"). Pagamentos anteriores à integração não são lançados como
-- realizados: o histórico de cada módulo continua nos seus próprios registros.

INSERT INTO bella.movimentacoes_caixa
    (id, tipo, descricao, categoria_id, valor, situacao, data_prevista, data_efetiva, origem,
     origem_referencia_id, observacoes, cancelada, criado_em, atualizado_em)
SELECT gen_random_uuid(),
       'SAIDA',
       'Conta a pagar: '
           || left(x.descricao, 200 - length('Conta a pagar: ') - length(x.sufixo)) || x.sufixo,
       NULL,
       x.saldo,
       'PREVISTO',
       x.vencimento,
       NULL,
       'CONTA_PAGAR',
       x.referencia,
       NULL,
       FALSE,
       now(),
       now()
FROM (
    SELECT c.descricao,
           ' (parcela ' || p.numero || '/' || (SELECT count(*) FROM bella.parcelas_conta_pagar t
                                                WHERE t.conta_pagar_id = c.id) || ')' AS sufixo,
           p.vencimento,
           p.valor - COALESCE((SELECT sum(g.valor_pago + g.desconto) FROM bella.pagamentos_conta_pagar g
                               WHERE g.parcela_id = p.id), 0) AS saldo,
           CASE
               WHEN n.pagamentos = 0 THEN p.id
               ELSE (substr(h.md5, 1, 8) || '-' || substr(h.md5, 9, 4) || '-3' || substr(h.md5, 14, 3) || '-'
                     || lpad(to_hex(((('x' || substr(h.md5, 17, 2))::bit(8)::int) & 63) | 128), 2, '0')
                     || substr(h.md5, 19, 2) || '-' || substr(h.md5, 21, 12))::uuid
           END AS referencia
    FROM bella.parcelas_conta_pagar p
    JOIN bella.contas_pagar c ON c.id = p.conta_pagar_id
    CROSS JOIN LATERAL (SELECT count(*) AS pagamentos FROM bella.pagamentos_conta_pagar g
                        WHERE g.parcela_id = p.id) n
    CROSS JOIN LATERAL (SELECT md5(p.id::text || '#' || n.pagamentos) AS md5) h
    WHERE c.status <> 'CANCELADA'
) x
WHERE x.saldo > 0
  AND NOT EXISTS (SELECT 1 FROM bella.movimentacoes_caixa m
                  WHERE m.origem = 'CONTA_PAGAR' AND m.origem_referencia_id = x.referencia);

INSERT INTO bella.movimentacoes_caixa
    (id, tipo, descricao, categoria_id, valor, situacao, data_prevista, data_efetiva, origem,
     origem_referencia_id, observacoes, cancelada, criado_em, atualizado_em)
SELECT gen_random_uuid(),
       'ENTRADA',
       'Conta a receber: '
           || left(x.descricao, 200 - length('Conta a receber: ') - length(x.sufixo)) || x.sufixo,
       NULL,
       x.saldo,
       'PREVISTO',
       x.vencimento,
       NULL,
       'CONTA_RECEBER',
       x.referencia,
       NULL,
       FALSE,
       now(),
       now()
FROM (
    SELECT c.descricao,
           ' (parcela ' || p.numero || '/' || (SELECT count(*) FROM bella.parcelas_conta_receber t
                                                WHERE t.conta_receber_id = c.id) || ')' AS sufixo,
           p.vencimento,
           p.valor - COALESCE((SELECT sum(g.valor_recebido + g.desconto) FROM bella.recebimentos_conta_receber g
                               WHERE g.parcela_id = p.id), 0) AS saldo,
           CASE
               WHEN n.recebimentos = 0 THEN p.id
               ELSE (substr(h.md5, 1, 8) || '-' || substr(h.md5, 9, 4) || '-3' || substr(h.md5, 14, 3) || '-'
                     || lpad(to_hex(((('x' || substr(h.md5, 17, 2))::bit(8)::int) & 63) | 128), 2, '0')
                     || substr(h.md5, 19, 2) || '-' || substr(h.md5, 21, 12))::uuid
           END AS referencia
    FROM bella.parcelas_conta_receber p
    JOIN bella.contas_receber c ON c.id = p.conta_receber_id
    CROSS JOIN LATERAL (SELECT count(*) AS recebimentos FROM bella.recebimentos_conta_receber g
                        WHERE g.parcela_id = p.id) n
    CROSS JOIN LATERAL (SELECT md5(p.id::text || '#' || n.recebimentos) AS md5) h
    WHERE c.status <> 'CANCELADA'
) x
WHERE x.saldo > 0
  AND NOT EXISTS (SELECT 1 FROM bella.movimentacoes_caixa m
                  WHERE m.origem = 'CONTA_RECEBER' AND m.origem_referencia_id = x.referencia);
