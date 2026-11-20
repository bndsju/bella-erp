package com.bella.backend.domain.usuario.port.in;

import java.util.UUID;

public interface AlterarMinhaSenhaUseCase {

    void alterar(UUID usuarioId, String senhaAtual, String novaSenha);
}
