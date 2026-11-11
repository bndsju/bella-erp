package com.bella.backend.adapter.out.persistence.usuario;

import com.bella.backend.domain.usuario.model.PerfilUsuario;
import com.bella.backend.domain.usuario.model.StatusUsuario;
import com.bella.backend.domain.usuario.model.Usuario;
import com.bella.backend.domain.usuario.port.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class UsuarioRepositoryAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository usuarioJpaRepository;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository usuarioJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        return paraDominio(usuarioJpaRepository.save(paraEntidade(usuario)));
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        return usuarioJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public Optional<Usuario> buscarPorLogin(String login) {
        return usuarioJpaRepository.findByLogin(login).map(this::paraDominio);
    }

    @Override
    public boolean existePorLogin(String login) {
        return usuarioJpaRepository.existsByLogin(login);
    }

    @Override
    public List<Usuario> listar(StatusUsuario status) {
        List<UsuarioJpaEntity> entidades = status == null
                ? usuarioJpaRepository.findAllByOrderByNome()
                : usuarioJpaRepository.findByStatusOrderByNome(status);
        return entidades.stream().map(this::paraDominio).toList();
    }

    @Override
    public long contarAdministradoresAtivos() {
        return usuarioJpaRepository.countByPerfilAndStatus(PerfilUsuario.ADMINISTRADOR, StatusUsuario.ATIVO);
    }

    @Override
    public long contar() {
        return usuarioJpaRepository.count();
    }

    private UsuarioJpaEntity paraEntidade(Usuario u) {
        return new UsuarioJpaEntity(u.getId(), u.getNome(), u.getLogin(), u.getSenhaHash(), u.getPerfil(),
                u.getStatus(), u.getSenhaAlteradaEm(), u.getUltimoAcessoEm(), u.isSenhaProvisoria(), u.getRedefinicaoSolicitadaEm(),
                u.getCriadoEm(), u.getAtualizadoEm());
    }

    private Usuario paraDominio(UsuarioJpaEntity e) {
        return Usuario.existente(e.getId(), e.getNome(), e.getLogin(), e.getSenhaHash(), e.getPerfil(), e.getStatus(),
                e.getSenhaAlteradaEm(), e.getUltimoAcessoEm(), e.isSenhaProvisoria(), e.getRedefinicaoSolicitadaEm(),
                e.getCriadoEm(), e.getAtualizadoEm());
    }
}
