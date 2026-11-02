package com.bella.backend.domain.auditoria.model;

import com.bella.backend.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistroAuditoriaTest {

    private RegistroAuditoria registro(String usuario, Map<String, Object> novos, String motivo) {
        return RegistroAuditoria.novo(ModuloAuditoria.CLIENTE, UUID.randomUUID(), OperacaoAuditoria.CRIACAO,
                usuario, null, novos, motivo);
    }

    @Test
    void criaRegistroComDataEIdentificadorGerados() {
        RegistroAuditoria registro = registro("  ana.souza ", Map.of("nome", "X"), "  motivo  ");

        assertThat(registro.id()).isNotNull();
        assertThat(registro.dataHora()).isNotNull();
        assertThat(registro.usuario()).isEqualTo("ana.souza");
        assertThat(registro.motivo()).isEqualTo("motivo");
    }

    @Test
    void usuarioEObrigatorioELimitado() {
        assertThatThrownBy(() -> registro(" ", null, null)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> registro(null, null, null)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> registro("u".repeat(101), null, null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void moduloRegistroEOperacaoSaoObrigatorios() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> RegistroAuditoria.novo(null, id, OperacaoAuditoria.CRIACAO, "u", null, null, null))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> RegistroAuditoria.novo(ModuloAuditoria.CLIENTE, null, OperacaoAuditoria.CRIACAO,
                "u", null, null, null)).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> RegistroAuditoria.novo(ModuloAuditoria.CLIENTE, id, null, "u", null, null, null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void motivoVazioViraNuloEMotivoLongoETruncado() {
        assertThat(registro("u", null, "   ").motivo()).isNull();
        assertThat(registro("u", null, "m".repeat(2000)).motivo()).hasSize(RegistroAuditoria.TAMANHO_MAXIMO_MOTIVO);
    }

    @Test
    void valoresSaoCopiadosEORegistroNaoPodeSerAlteradoPorFora() {
        Map<String, Object> original = new HashMap<>();
        original.put("campo", "valor");
        RegistroAuditoria registro = registro("u", original, null);

        original.put("campo", "adulterado");

        assertThat(registro.novosValores()).containsEntry("campo", "valor");
        assertThatThrownBy(() -> registro.novosValores().put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
