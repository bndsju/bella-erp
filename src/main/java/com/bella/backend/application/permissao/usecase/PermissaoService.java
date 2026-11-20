package com.bella.backend.application.permissao.usecase;

import com.bella.backend.domain.permissao.model.AcaoPermissao;
import com.bella.backend.domain.permissao.model.ModuloSistema;
import com.bella.backend.domain.permissao.model.PermissoesPadrao;
import com.bella.backend.domain.permissao.port.in.AlterarPermissoesUseCase;
import com.bella.backend.domain.permissao.port.in.ConsultarPermissoesUseCase;
import com.bella.backend.domain.permissao.port.out.PermissaoRepositoryPort;
import com.bella.backend.domain.shared.RegraDeNegocioException;
import com.bella.backend.domain.usuario.model.PerfilUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Guarda as permissões em memória: cada requisição à API consulta isto, então ir ao banco toda vez seria caro.
 * O cache é descartado depois de cada alteração confirmada e recarregado na próxima consulta. Com mais de uma
 * instância do servidor seria preciso um cache compartilhado ou com validade curta.
 */
@Service
@Transactional
public class PermissaoService implements ConsultarPermissoesUseCase, AlterarPermissoesUseCase {

    private static final Logger LOG = LoggerFactory.getLogger(PermissaoService.class);

    private final PermissaoRepositoryPort permissaoRepositoryPort;
    private volatile Map<PerfilUsuario, Map<ModuloSistema, Set<AcaoPermissao>>> cache;

    public PermissaoService(PermissaoRepositoryPort permissaoRepositoryPort) {
        this.permissaoRepositoryPort = permissaoRepositoryPort;
    }

    @Override
    // SUPPORTS: ler do cache não deve abrir transação (nem pegar conexão) a cada chamada da API
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Map<ModuloSistema, Set<AcaoPermissao>> doPerfil(PerfilUsuario perfil) {
        return todas().get(perfil);
    }

    @Override
    // SUPPORTS: ler do cache não deve abrir transação (nem pegar conexão) a cada chamada da API
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Map<PerfilUsuario, Map<ModuloSistema, Set<AcaoPermissao>>> todas() {
        Map<PerfilUsuario, Map<ModuloSistema, Set<AcaoPermissao>>> atual = cache;
        if (atual == null) {
            atual = carregar();
            cache = atual;
        }
        return atual;
    }

    @Override
    // SUPPORTS: ler do cache não deve abrir transação (nem pegar conexão) a cada chamada da API
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean permite(PerfilUsuario perfil, ModuloSistema modulo, AcaoPermissao acao) {
        Set<AcaoPermissao> acoes = doPerfil(perfil).get(modulo);
        return acoes != null && acoes.contains(acao);
    }

    @Override
    public Map<ModuloSistema, Set<AcaoPermissao>> alterar(PerfilUsuario perfil,
                                                          Map<ModuloSistema, Set<AcaoPermissao>> permissoes) {
        exigirPerfilEditavel(perfil);
        Map<ModuloSistema, Set<AcaoPermissao>> normalizadas = normalizar(permissoes);
        permissaoRepositoryPort.substituir(perfil, normalizadas);
        descartarCacheAposConfirmar();
        LOG.info("Permissões do perfil {} alteradas: {}", perfil, normalizadas);
        return normalizadas;
    }

    @Override
    public Map<ModuloSistema, Set<AcaoPermissao>> restaurarPadrao(PerfilUsuario perfil) {
        exigirPerfilEditavel(perfil);
        Map<ModuloSistema, Set<AcaoPermissao>> padrao = PermissoesPadrao.de(perfil);
        permissaoRepositoryPort.substituir(perfil, padrao);
        descartarCacheAposConfirmar();
        LOG.info("Permissões do perfil {} restauradas para o padrão", perfil);
        return padrao;
    }

    private Map<PerfilUsuario, Map<ModuloSistema, Set<AcaoPermissao>>> carregar() {
        Map<PerfilUsuario, Map<ModuloSistema, Set<AcaoPermissao>>> doBanco = permissaoRepositoryPort.carregarTodas();
        Map<PerfilUsuario, Map<ModuloSistema, Set<AcaoPermissao>>> todas = new EnumMap<>(PerfilUsuario.class);
        for (PerfilUsuario perfil : PerfilUsuario.values()) {
            todas.put(perfil, perfil == PerfilUsuario.ADMINISTRADOR
                    ? PermissoesPadrao.de(PerfilUsuario.ADMINISTRADOR)
                    : doBanco.getOrDefault(perfil, Map.of()));
        }
        return todas;
    }

    private static void exigirPerfilEditavel(PerfilUsuario perfil) {
        if (perfil == null) {
            throw new RegraDeNegocioException("Perfil é obrigatório");
        }
        if (perfil == PerfilUsuario.ADMINISTRADOR) {
            throw new RegraDeNegocioException("O administrador sempre tem acesso total e não pode ser alterado");
        }
    }

    private static Map<ModuloSistema, Set<AcaoPermissao>> normalizar(Map<ModuloSistema, Set<AcaoPermissao>> pedidas) {
        Map<ModuloSistema, Set<AcaoPermissao>> resultado = new EnumMap<>(ModuloSistema.class);
        if (pedidas == null) {
            return resultado;
        }
        pedidas.forEach((modulo, acoes) -> {
            if (acoes == null || acoes.isEmpty()) {
                return;
            }
            for (AcaoPermissao acao : acoes) {
                if (!modulo.temAcao(acao)) {
                    throw new RegraDeNegocioException(
                            "A ação \"" + acao.rotulo() + "\" não existe em " + modulo.rotulo());
                }
            }
            Set<AcaoPermissao> completas = EnumSet.copyOf(acoes);
            completas.add(AcaoPermissao.VER); // quem cria, edita ou cancela precisa enxergar
            resultado.put(modulo, completas);
        });
        return resultado;
    }

    /** Só descarta depois do commit: descartar antes deixaria uma leitura concorrente recarregar o estado antigo. */
    private void descartarCacheAposConfirmar() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cache = null;
                }
            });
        } else {
            cache = null;
        }
    }
}
