package com.bella.backend.adapter.in.web.usuario;

import com.bella.backend.adapter.in.web.seguranca.UsuarioAutenticado;
import com.bella.backend.adapter.in.web.permissao.PermissaoController;
import com.bella.backend.domain.permissao.port.in.ConsultarPermissoesUseCase;
import com.bella.backend.domain.usuario.port.in.AlterarMinhaSenhaUseCase;
import com.bella.backend.domain.usuario.port.in.AutenticarUseCase;
import com.bella.backend.domain.usuario.port.in.GerenciarUsuariosUseCase;
import com.bella.backend.domain.usuario.port.in.SolicitarRedefinicaoSenhaUseCase;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticarUseCase autenticarUseCase;
    private final AlterarMinhaSenhaUseCase alterarMinhaSenhaUseCase;
    private final GerenciarUsuariosUseCase gerenciarUsuariosUseCase;
    private final SolicitarRedefinicaoSenhaUseCase solicitarRedefinicaoSenhaUseCase;
    private final ConsultarPermissoesUseCase consultarPermissoesUseCase;

    public AuthController(AutenticarUseCase autenticarUseCase, AlterarMinhaSenhaUseCase alterarMinhaSenhaUseCase,
                          GerenciarUsuariosUseCase gerenciarUsuariosUseCase,
                          SolicitarRedefinicaoSenhaUseCase solicitarRedefinicaoSenhaUseCase,
                          ConsultarPermissoesUseCase consultarPermissoesUseCase) {
        this.consultarPermissoesUseCase = consultarPermissoesUseCase;
        this.solicitarRedefinicaoSenhaUseCase = solicitarRedefinicaoSenhaUseCase;
        this.autenticarUseCase = autenticarUseCase;
        this.alterarMinhaSenhaUseCase = alterarMinhaSenhaUseCase;
        this.gerenciarUsuariosUseCase = gerenciarUsuariosUseCase;
    }

    public record SessaoResponse(String token, Instant expiraEm, UsuarioResponse usuario) {
    }

    @PostMapping("/login")
    public SessaoResponse login(@Valid @RequestBody UsuarioRequests.Login request, HttpServletRequest http) {
        AutenticarUseCase.Sessao sessao = autenticarUseCase.autenticar(
                new AutenticarUseCase.Comando(request.login(), request.senha(), http.getRemoteAddr()));
        return new SessaoResponse(sessao.token(), sessao.expiraEm(), UsuarioResponse.de(sessao.usuario()));
    }

    public record MensagemResponse(String mensagem) {
    }

    /** Resposta idêntica para login existente ou não, para não revelar quem tem cadastro. */
    @PostMapping("/esqueci-senha")
    public ResponseEntity<MensagemResponse> esqueciSenha(@Valid @RequestBody UsuarioRequests.EsqueciSenha request,
                                                         HttpServletRequest http) {
        solicitarRedefinicaoSenhaUseCase.solicitar(request.login(), http.getRemoteAddr());
        return ResponseEntity.accepted().body(new MensagemResponse(
                "Pedido registrado. Se o login existir, o administrador vai gerar uma senha provisória e passar para você."));
    }

    @GetMapping("/me")
    public UsuarioResponse eu(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return UsuarioResponse.de(gerenciarUsuariosUseCase.buscarPorId(autenticado.id()));
    }

    public record MinhasPermissoesResponse(Map<String, List<String>> permissoes) {
    }

    /** O que o usuário logado pode fazer, para a tela esconder o que ele não pode usar. */
    @GetMapping("/permissoes")
    public MinhasPermissoesResponse minhasPermissoes(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return new MinhasPermissoesResponse(PermissaoController.paraTexto(
                consultarPermissoesUseCase.doPerfil(autenticado.perfil())));
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(@AuthenticationPrincipal UsuarioAutenticado autenticado,
                                             @Valid @RequestBody UsuarioRequests.AlterarMinhaSenha request) {
        alterarMinhaSenhaUseCase.alterar(autenticado.id(), request.senhaAtual(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }
}
