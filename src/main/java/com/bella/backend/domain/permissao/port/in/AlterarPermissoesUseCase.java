package com.bella.backend.domain.permissao.port.in;

import com.bella.backend.domain.permissao.model.AcaoPermissao;
import com.bella.backend.domain.permissao.model.ModuloSistema;
import com.bella.backend.domain.usuario.model.PerfilUsuario;

import java.util.Map;
import java.util.Set;

public interface AlterarPermissoesUseCase {

    /**
     * Substitui as permissões do perfil. Ações que o módulo não tem são recusadas; criar, editar e cancelar sempre
     * levam junto o "ver" (não faz sentido editar o que não se enxerga). O administrador não pode ser alterado.
     */
    Map<ModuloSistema, Set<AcaoPermissao>> alterar(PerfilUsuario perfil, Map<ModuloSistema, Set<AcaoPermissao>> permissoes);

    Map<ModuloSistema, Set<AcaoPermissao>> restaurarPadrao(PerfilUsuario perfil);
}
