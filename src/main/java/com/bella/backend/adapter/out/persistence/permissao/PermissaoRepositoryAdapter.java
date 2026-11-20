package com.bella.backend.adapter.out.persistence.permissao;

import com.bella.backend.domain.permissao.model.AcaoPermissao;
import com.bella.backend.domain.permissao.model.ModuloSistema;
import com.bella.backend.domain.permissao.port.out.PermissaoRepositoryPort;
import com.bella.backend.domain.usuario.model.PerfilUsuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Uma linha por (perfil, módulo, ação) concedida; sem linha, não tem permissão. */
@Component
public class PermissaoRepositoryAdapter implements PermissaoRepositoryPort {

    private final JdbcTemplate jdbc;

    public PermissaoRepositoryAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Map<PerfilUsuario, Map<ModuloSistema, Set<AcaoPermissao>>> carregarTodas() {
        Map<PerfilUsuario, Map<ModuloSistema, Set<AcaoPermissao>>> resultado = new EnumMap<>(PerfilUsuario.class);
        jdbc.query("SELECT perfil, modulo, acao FROM bella.permissoes_perfil", rs -> {
            // linhas de módulo ou ação que não existem mais no código são ignoradas, em vez de derrubar o login de todos
            try {
                PerfilUsuario perfil = PerfilUsuario.valueOf(rs.getString("perfil"));
                ModuloSistema modulo = ModuloSistema.valueOf(rs.getString("modulo"));
                AcaoPermissao acao = AcaoPermissao.valueOf(rs.getString("acao"));
                resultado.computeIfAbsent(perfil, p -> new EnumMap<>(ModuloSistema.class))
                        .computeIfAbsent(modulo, m -> EnumSet.noneOf(AcaoPermissao.class))
                        .add(acao);
            } catch (IllegalArgumentException ignorada) {
                // valor desconhecido
            }
        });
        return resultado;
    }

    @Override
    public void substituir(PerfilUsuario perfil, Map<ModuloSistema, Set<AcaoPermissao>> permissoes) {
        jdbc.update("DELETE FROM bella.permissoes_perfil WHERE perfil = ?", perfil.name());
        List<Object[]> linhas = new ArrayList<>();
        permissoes.forEach((modulo, acoes) -> acoes.forEach(
                acao -> linhas.add(new Object[]{perfil.name(), modulo.name(), acao.name()})));
        if (!linhas.isEmpty()) {
            jdbc.batchUpdate("INSERT INTO bella.permissoes_perfil (perfil, modulo, acao) VALUES (?, ?, ?)", linhas);
        }
    }
}
