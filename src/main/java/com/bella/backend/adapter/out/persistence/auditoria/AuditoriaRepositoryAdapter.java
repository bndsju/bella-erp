package com.bella.backend.adapter.out.persistence.auditoria;

import com.bella.backend.domain.auditoria.model.RegistroAuditoria;
import com.bella.backend.domain.auditoria.port.in.ConsultarAuditoriaUseCase.FiltroAuditoria;
import com.bella.backend.domain.auditoria.port.out.AuditoriaRepositoryPort;
import com.bella.backend.domain.relatorio.model.Pagina;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AuditoriaRepositoryAdapter implements AuditoriaRepositoryPort {

    private final AuditoriaJpaRepository auditoriaJpaRepository;

    public AuditoriaRepositoryAdapter(AuditoriaJpaRepository auditoriaJpaRepository) {
        this.auditoriaJpaRepository = auditoriaJpaRepository;
    }

    @Override
    public RegistroAuditoria salvar(RegistroAuditoria registro) {
        return paraDominio(auditoriaJpaRepository.save(new AuditoriaJpaEntity(
                registro.id(), registro.modulo(), registro.entidadeId(), registro.operacao(), registro.usuario(),
                registro.dataHora(), registro.valoresAnteriores(), registro.novosValores(), registro.motivo())));
    }

    @Override
    public Optional<RegistroAuditoria> buscarPorId(UUID id) {
        return auditoriaJpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public Pagina<RegistroAuditoria> buscar(FiltroAuditoria filtro, int pagina, int tamanho) {
        PageRequest pedido = PageRequest.of(pagina, tamanho,
                Sort.by(Sort.Order.desc("dataHora"), Sort.Order.desc("id")));
        Page<AuditoriaJpaEntity> resultado = auditoriaJpaRepository.findAll(especificacao(filtro), pedido);
        return new Pagina<>(resultado.getContent().stream().map(this::paraDominio).toList(), pagina, tamanho,
                resultado.getTotalElements(), resultado.getTotalPages());
    }

    private Specification<AuditoriaJpaEntity> especificacao(FiltroAuditoria filtro) {
        return (raiz, consulta, builder) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (filtro.usuario() != null && !filtro.usuario().isBlank()) {
                condicoes.add(builder.equal(builder.lower(raiz.get("usuario")),
                        filtro.usuario().trim().toLowerCase()));
            }
            if (filtro.modulo() != null) {
                condicoes.add(builder.equal(raiz.get("modulo"), filtro.modulo()));
            }
            if (filtro.operacao() != null) {
                condicoes.add(builder.equal(raiz.get("operacao"), filtro.operacao()));
            }
            if (filtro.entidadeId() != null) {
                condicoes.add(builder.equal(raiz.get("entidadeId"), filtro.entidadeId()));
            }
            if (filtro.periodo() != null) {
                Instant inicio = filtro.periodo().inicioComoInstante();
                Instant fimExclusivo = filtro.periodo().fimComoInstante();
                if (inicio != null) {
                    condicoes.add(builder.greaterThanOrEqualTo(raiz.<Instant>get("dataHora"), inicio));
                }
                if (fimExclusivo != null) {
                    condicoes.add(builder.lessThan(raiz.<Instant>get("dataHora"), fimExclusivo));
                }
            }
            return builder.and(condicoes.toArray(new Predicate[0]));
        };
    }

    private RegistroAuditoria paraDominio(AuditoriaJpaEntity entidade) {
        return new RegistroAuditoria(entidade.getId(), entidade.getModulo(), entidade.getEntidadeId(),
                entidade.getOperacao(), entidade.getUsuario(), entidade.getDataHora(),
                entidade.getValoresAnteriores(), entidade.getNovosValores(), entidade.getMotivo());
    }
}
