-- Dados específicos de Pessoa Física, em relação 1:1 com clientes.
-- cliente_id é ao mesmo tempo PK e FK: cada cliente PF tem exatamente uma linha aqui.

CREATE TABLE bella.clientes_pf (
    cliente_id          UUID PRIMARY KEY REFERENCES bella.clientes (id) ON DELETE CASCADE,
    nome_completo        VARCHAR(150) NOT NULL,
    data_nascimento      DATE NOT NULL,
    cpf                  VARCHAR(11) NOT NULL UNIQUE
);

CREATE INDEX idx_clientes_pf_nome_completo ON bella.clientes_pf (nome_completo);
