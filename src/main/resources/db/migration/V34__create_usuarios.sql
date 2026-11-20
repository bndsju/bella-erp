CREATE TABLE bella.usuarios (
    id                UUID PRIMARY KEY,
    nome              VARCHAR(100) NOT NULL,
    login             VARCHAR(50)  NOT NULL,
    senha_hash        VARCHAR(100) NOT NULL,
    perfil            VARCHAR(20)  NOT NULL CHECK (perfil IN ('ADMINISTRADOR', 'GERENTE', 'FINANCEIRO', 'VENDEDOR')),
    status            VARCHAR(10)  NOT NULL DEFAULT 'ATIVO' CHECK (status IN ('ATIVO', 'INATIVO')),
    senha_alterada_em TIMESTAMPTZ  NOT NULL,
    ultimo_acesso_em  TIMESTAMPTZ,
    criado_em         TIMESTAMPTZ  NOT NULL,
    atualizado_em     TIMESTAMPTZ  NOT NULL
);

CREATE UNIQUE INDEX uk_usuarios_login ON bella.usuarios (login);
