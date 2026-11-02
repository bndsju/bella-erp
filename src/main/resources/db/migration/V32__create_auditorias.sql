CREATE TABLE bella.auditorias (
    id                 UUID PRIMARY KEY,
    modulo             VARCHAR(30)  NOT NULL,
    entidade_id        UUID         NOT NULL,
    operacao           VARCHAR(30)  NOT NULL,
    usuario            VARCHAR(100) NOT NULL,
    data_hora          TIMESTAMPTZ  NOT NULL,
    valores_anteriores JSONB,
    novos_valores      JSONB,
    motivo             VARCHAR(1000)
);

CREATE INDEX idx_auditorias_entidade ON bella.auditorias (modulo, entidade_id, data_hora DESC);
CREATE INDEX idx_auditorias_usuario ON bella.auditorias (lower(usuario), data_hora DESC);
CREATE INDEX idx_auditorias_operacao ON bella.auditorias (operacao, data_hora DESC);
CREATE INDEX idx_auditorias_data_hora ON bella.auditorias (data_hora DESC);

-- A auditoria só aceita inclusão: nenhuma operação comum da aplicação pode alterar ou apagar o histórico.
CREATE FUNCTION bella.bloquear_alteracao_auditoria() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Registros de auditoria são imutáveis e não podem ser alterados ou excluídos';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_auditorias_imutavel
    BEFORE UPDATE OR DELETE ON bella.auditorias
    FOR EACH ROW EXECUTE FUNCTION bella.bloquear_alteracao_auditoria();

CREATE TRIGGER trg_auditorias_sem_truncate
    BEFORE TRUNCATE ON bella.auditorias
    FOR EACH STATEMENT EXECUTE FUNCTION bella.bloquear_alteracao_auditoria();
