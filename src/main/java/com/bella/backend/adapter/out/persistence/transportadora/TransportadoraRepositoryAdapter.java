package com.bella.backend.adapter.out.persistence.transportadora;

import com.bella.backend.domain.transportadora.model.StatusTransportadora;
import com.bella.backend.domain.transportadora.model.Transportadora;
import com.bella.backend.domain.transportadora.port.out.TransportadoraRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TransportadoraRepositoryAdapter implements TransportadoraRepositoryPort {

    private final TransportadoraJpaRepository transportadoraJpaRepository;

    public TransportadoraRepositoryAdapter(TransportadoraJpaRepository transportadoraJpaRepository) {
        this.transportadoraJpaRepository = transportadoraJpaRepository;
    }

    @Override
    public Transportadora salvar(Transportadora transportadora) {
        TransportadoraJpaEntity salva = transportadoraJpaRepository.save(paraEntidade(transportadora));
        return paraDominio(salva);
    }

    @Override
    public Optional<Transportadora> buscarPorId(UUID id) {
        return transportadoraJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Transportadora> listar(StatusTransportadora status) {
        List<TransportadoraJpaEntity> entidades = status == null
                ? transportadoraJpaRepository.findAllByOrderByNome()
                : transportadoraJpaRepository.findByStatusOrderByNome(status);
        return entidades.stream().map(this::paraDominio).toList();
    }

    @Override
    public boolean existePorId(UUID id) {
        return transportadoraJpaRepository.existsById(id);
    }

    private TransportadoraJpaEntity paraEntidade(Transportadora transportadora) {
        return new TransportadoraJpaEntity(transportadora.getId(), transportadora.getNome(),
                transportadora.getTelefone(), transportadora.getStatus(), transportadora.getCriadoEm(),
                transportadora.getAtualizadoEm());
    }

    private Transportadora paraDominio(TransportadoraJpaEntity entidade) {
        return Transportadora.existente(entidade.getId(), entidade.getNome(), entidade.getTelefone(),
                entidade.getStatus(), entidade.getCriadoEm(), entidade.getAtualizadoEm());
    }
}
