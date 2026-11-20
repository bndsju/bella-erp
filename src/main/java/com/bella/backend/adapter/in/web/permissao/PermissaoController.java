package com.bella.backend.adapter.in.web.permissao;

import com.bella.backend.domain.permissao.model.AcaoPermissao;
import com.bella.backend.domain.permissao.model.ModuloSistema;
import com.bella.backend.domain.permissao.port.in.AlterarPermissoesUseCase;
import com.bella.backend.domain.permissao.port.in.ConsultarPermissoesUseCase;
import com.bella.backend.domain.usuario.model.PerfilUsuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Matriz de permissões por perfil. Acesso restrito ao ADMINISTRADOR (ver SegurancaConfig). */
@RestController
@RequestMapping("/api/permissoes")
public class PermissaoController {

    private final ConsultarPermissoesUseCase consultarPermissoesUseCase;
    private final AlterarPermissoesUseCase alterarPermissoesUseCase;

    public PermissaoController(ConsultarPermissoesUseCase consultarPermissoesUseCase,
                               AlterarPermissoesUseCase alterarPermissoesUseCase) {
        this.consultarPermissoesUseCase = consultarPermissoesUseCase;
        this.alterarPermissoesUseCase = alterarPermissoesUseCase;
    }

    public record ModuloResponse(String chave, String rotulo, String grupo, List<String> acoes) {
    }

    public record AcaoResponse(String chave, String rotulo) {
    }

    public record PerfilResponse(PerfilUsuario perfil, boolean fixo, Map<String, List<String>> permissoes) {
    }

    public record MatrizResponse(List<AcaoResponse> acoes, List<ModuloResponse> modulos, List<PerfilResponse> perfis) {
    }

    public record AlterarRequest(@NotNull(message = "Permissões são obrigatórias")
                                 Map<ModuloSistema, Set<AcaoPermissao>> permissoes) {
    }

    @GetMapping
    public MatrizResponse matriz() {
        List<AcaoResponse> acoes = Arrays.stream(AcaoPermissao.values())
                .map(a -> new AcaoResponse(a.name(), a.rotulo())).toList();
        List<ModuloResponse> modulos = Arrays.stream(ModuloSistema.values())
                .map(m -> new ModuloResponse(m.name(), m.rotulo(), m.grupo(),
                        m.acoesDisponiveis().stream().map(Enum::name).toList()))
                .toList();
        List<PerfilResponse> perfis = Arrays.stream(PerfilUsuario.values())
                .map(p -> resposta(p, consultarPermissoesUseCase.doPerfil(p)))
                .toList();
        return new MatrizResponse(acoes, modulos, perfis);
    }

    @PutMapping("/{perfil}")
    public PerfilResponse alterar(@PathVariable PerfilUsuario perfil, @Valid @RequestBody AlterarRequest request) {
        return resposta(perfil, alterarPermissoesUseCase.alterar(perfil, request.permissoes()));
    }

    @PostMapping("/{perfil}/restaurar-padrao")
    public PerfilResponse restaurarPadrao(@PathVariable PerfilUsuario perfil) {
        return resposta(perfil, alterarPermissoesUseCase.restaurarPadrao(perfil));
    }

    static PerfilResponse resposta(PerfilUsuario perfil, Map<ModuloSistema, Set<AcaoPermissao>> permissoes) {
        return new PerfilResponse(perfil, perfil == PerfilUsuario.ADMINISTRADOR, paraTexto(permissoes));
    }

    public static Map<String, List<String>> paraTexto(Map<ModuloSistema, Set<AcaoPermissao>> permissoes) {
        Map<String, List<String>> texto = new LinkedHashMap<>();
        for (ModuloSistema modulo : ModuloSistema.values()) {
            Set<AcaoPermissao> acoes = permissoes.get(modulo);
            if (acoes != null && !acoes.isEmpty()) {
                texto.put(modulo.name(), acoes.stream().map(Enum::name).toList());
            }
        }
        return texto;
    }
}
