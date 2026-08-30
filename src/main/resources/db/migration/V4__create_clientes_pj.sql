-- Dados específicos de Pessoa Jurídica, em relação 1:1 com clientes.
-- cliente_id é ao mesmo tempo PK e FK: cada cliente PJ tem exatamente uma linha aqui.

CREATE TABLE bella.clientes_pj (
    cliente_id           UUID PRIMARY KEY REFERENCES bella.clientes (id) ON DELETE CASCADE,
    razao_social         VARCHAR(150) NOT NULL,
    nome_fantasia        VARCHAR(150),
    cnpj                 VARCHAR(14) NOT NULL UNIQUE,
    inscricao_estadual   VARCHAR(20),
    inscricao_municipal  VARCHAR(20)
);

CREATE INDEX idx_clientes_pj_razao_social ON bella.clientes_pj (razao_social);
