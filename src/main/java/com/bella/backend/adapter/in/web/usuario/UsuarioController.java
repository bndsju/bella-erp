package com.bella.backend.adapter.in.web.usuario;

import com.bella.backend.adapter.in.web.seguranca.UsuarioAutenticado;
import com.bella.backend.domain.usuario.model.StatusUsuario;
import com.bella.backend.domain.usuario.model.Usuario;
import com.bella.backend.domain.usuario.port.in.GerenciarUsuariosUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/** Acesso restrito ao perfil ADMINISTRADOR (ver SegurancaConfig). */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final GerenciarUsuariosUseCase gerenciarUsuariosUseCase;

    public UsuarioController(GerenciarUsuariosUseCase gerenciarUsuariosUseCase) {
        this.gerenciarUsuariosUseCase = gerenciarUsuariosUseCase;
    }

    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) StatusUsuario status) {
        return gerenciarUsuariosUseCase.listar(status).stream().map(UsuarioResponse::de).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable UUID id) {
        return UsuarioResponse.de(gerenciarUsuariosUseCase.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioRequests.Cadastrar request) {
        Usuario usuario = gerenciarUsuariosUseCase.cadastrar(new GerenciarUsuariosUseCase.CadastrarComando(
                request.nome(), request.login(), request.senha(), request.perfil()));
        return ResponseEntity.created(URI.create("/api/usuarios/" + usuario.getId())).body(UsuarioResponse.de(usuario));
    }

    @PutMapping("/{id}")
    public UsuarioResponse editar(@AuthenticationPrincipal UsuarioAutenticado executor, @PathVariable UUID id,
                                  @Valid @RequestBody UsuarioRequests.Editar request) {
        return UsuarioResponse.de(gerenciarUsuariosUseCase.editar(executor.id(), id,
                new GerenciarUsuariosUseCase.EditarComando(request.nome(), request.perfil())));
    }

    @PatchMapping("/{id}/status")
    public UsuarioResponse alterarStatus(@AuthenticationPrincipal UsuarioAutenticado executor, @PathVariable UUID id,
                                         @Valid @RequestBody UsuarioRequests.AlterarStatus request) {
        return UsuarioResponse.de(gerenciarUsuariosUseCase.alterarStatus(executor.id(), id, request.status()));
    }

    public record SenhaProvisoriaResponse(String senhaProvisoria) {
    }

    /** A senha só aparece nesta resposta; depois dela só existe o hash. */
    @PostMapping("/{id}/senha-provisoria")
    public SenhaProvisoriaResponse gerarSenhaProvisoria(@AuthenticationPrincipal UsuarioAutenticado executor,
                                                        @PathVariable UUID id) {
        return new SenhaProvisoriaResponse(gerenciarUsuariosUseCase.gerarSenhaProvisoria(executor.id(), id));
    }
}
