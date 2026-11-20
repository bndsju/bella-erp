package com.bella.backend.domain.permissao.port.out;

import com.bella.backend.domain.permissao.model.AcaoPermissao;
import com.bella.backend.domain.permissao.model.ModuloSistema;
import com.bella.backend.domain.usuario.model.PerfilUsuario;

import java.util.Map;
import java.util.Set;

public interface PermissaoRepositoryPort {

    Map<PerfilUsuario, Map<ModuloSistema, Set<AcaoPermissao>>> carregarTodas();

    /** Troca tudo o que o perfil tinha pelas permissões informadas. */
    void substituir(PerfilUsuario perfil, Map<ModuloSistema, Set<AcaoPermissao>> permissoes);
}
