ALTER TABLE bella.usuarios
    ADD COLUMN senha_provisoria          BOOLEAN     NOT NULL DEFAULT FALSE,
    ADD COLUMN redefinicao_solicitada_em TIMESTAMPTZ;
