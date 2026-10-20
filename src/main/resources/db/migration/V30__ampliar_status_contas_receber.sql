-- PARCIALMENTE_RECEBIDA tem 21 caracteres e não cabia em VARCHAR(20).
ALTER TABLE bella.contas_receber ALTER COLUMN status TYPE VARCHAR(30);
ALTER TABLE bella.parcelas_conta_receber ALTER COLUMN status TYPE VARCHAR(30);
