package com.bella.backend.adapter.in.web.usuario;

import com.bella.backend.adapter.in.web.seguranca.UsuarioAutenticado;
import com.bella.backend.domain.usuario.port.in.AlterarMinhaSenhaUseCase;
import com.bella.backend.domain.usuario.port.in.AutenticarUseCase;
import com.bella.backend.domain.usuario.port.in.GerenciarUsuariosUseCase;
import com.bella.backend.domain.usuario.port.in.SolicitarRedefinicaoSenhaUseCase;
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AutenticarUseCase autenticarUseCase;
    private final AlterarMinhaSenhaUseCase alterarMinhaSenhaUseCase;
    private final GerenciarUsuariosUseCase gerenciarUsuariosUseCase;
    private final SolicitarRedefinicaoSenhaUseCase solicitarRedefinicaoSenhaUseCase;

    public AuthController(AutenticarUseCase autenticarUseCase, AlterarMinhaSenhaUseCase alterarMinhaSenhaUseCase,
                          GerenciarUsuariosUseCase gerenciarUsuariosUseCase,
                          SolicitarRedefinicaoSenhaUseCase solicitarRedefinicaoSenhaUseCase) {
        this.solicitarRedefinicaoSenhaUseCase = solicitarRedefinicaoSenhaUseCase;
        this.autenticarUseCase = autenticarUseCase;
        this.alterarMinhaSenhaUseCase = alterarMinhaSenhaUseCase;
        this.gerenciarUsuariosUseCase = gerenciarUsuariosUseCase;
    }

    public record SessaoResponse(String token, Instant expiraEm, UsuarioResponse usuario) {
    }

    @PostMapping("/login")
    public SessaoResponse login(@Valid @RequestBody UsuarioRequests.Login request) {
        AutenticarUseCase.Sessao sessao = autenticarUseCase.autenticar(
                new AutenticarUseCase.Comando(request.login(), request.senha()));
        return new SessaoResponse(sessao.token(), sessao.expiraEm(), UsuarioResponse.de(sessao.usuario()));
    }

    public record MensagemResponse(String mensagem) {
    }

    /** Resposta idêntica para login existente ou não, para não revelar quem tem cadastro. */
    @PostMapping("/esqueci-senha")
    public ResponseEntity<MensagemResponse> esqueciSenha(@Valid @RequestBody UsuarioRequests.EsqueciSenha request) {
        solicitarRedefinicaoSenhaUseCase.solicitar(request.login());
        return ResponseEntity.accepted().body(new MensagemResponse(
                "Pedido registrado. Se o login existir, o administrador vai gerar uma senha provisória e passar para você."));
    }

    @GetMapping("/me")
    public UsuarioResponse eu(@AuthenticationPrincipal UsuarioAutenticado autenticado) {
        return UsuarioResponse.de(gerenciarUsuariosUseCase.buscarPorId(autenticado.id()));
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> alterarSenha(@AuthenticationPrincipal UsuarioAutenticado autenticado,
                                             @Valid @RequestBody UsuarioRequests.AlterarMinhaSenha request) {
        alterarMinhaSenhaUseCase.alterar(autenticado.id(), request.senhaAtual(), request.novaSenha());
        return ResponseEntity.noContent().build();
    }
}
