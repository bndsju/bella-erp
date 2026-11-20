package com.bella.backend.domain.permissao.model;

import com.bella.backend.domain.usuario.model.PerfilUsuario;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Permissões com que cada perfil nasce (e para as quais "restaurar padrão" volta). O administrador não entra aqui:
 * ele sempre tem tudo e isso não é configurável.
 * <ul>
 *   <li>Gerente: tudo.</li>
 *   <li>Financeiro: tudo, menos a auditoria.</li>
 *   <li>Vendedor: o dia a dia (cadastros, comercial, compras e estoque) e o painel; sem financeiro, relatórios e auditoria.</li>
 * </ul>
 */
public final class PermissoesPadrao {

    private static final Set<ModuloSistema> FORA_DO_VENDEDOR = EnumSet.of(
            ModuloSistema.CONTAS_RECEBER, ModuloSistema.CONTAS_PAGAR, ModuloSistema.FLUXO_CAIXA,
            ModuloSistema.RELATORIOS, ModuloSistema.AUDITORIA);

    private PermissoesPadrao() {
    }

    public static Map<ModuloSistema, Set<AcaoPermissao>> de(PerfilUsuario perfil) {
        Map<ModuloSistema, Set<AcaoPermissao>> mapa = new EnumMap<>(ModuloSistema.class);
        for (ModuloSistema modulo : ModuloSistema.values()) {
            boolean concede = switch (perfil) {
                case ADMINISTRADOR, GERENTE -> true;
                case FINANCEIRO -> modulo != ModuloSistema.AUDITORIA;
                case VENDEDOR -> !FORA_DO_VENDEDOR.contains(modulo);
            };
            if (concede) {
                mapa.put(modulo, EnumSet.copyOf(modulo.acoesDisponiveis()));
            }
        }
        return mapa;
    }
}
