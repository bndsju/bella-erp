package com.bella.backend.config;

import com.bella.backend.domain.usuario.model.PerfilUsuario;
import com.bella.backend.domain.usuario.model.SenhaProvisoria;
import com.bella.backend.domain.usuario.model.Usuario;
import com.bella.backend.domain.usuario.port.in.GerenciarUsuariosUseCase;
import com.bella.backend.domain.usuario.port.out.UsuarioRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cria o primeiro administrador quando o banco ainda não tem nenhum usuário, senão ninguém conseguiria entrar.
 * A senha vem de {@code ADMIN_SENHA}; se não for informada, uma senha aleatória é gerada e escrita uma única vez
 * no log de inicialização.
 */
@Configuration
public class AdministradorInicialConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AdministradorInicialConfig.class);

    @Bean
    ApplicationRunner criarAdministradorInicial(UsuarioRepositoryPort usuarios, GerenciarUsuariosUseCase gerenciar,
                                                @Value("${bella.seguranca.habilitada:true}") boolean habilitada,
                                                @Value("${bella.seguranca.admin-login:admin}") String login,
                                                @Value("${bella.seguranca.admin-nome:Administrador}") String nome,
                                                @Value("${bella.seguranca.admin-senha:}") String senhaInformada) {
        return args -> {
            if (!habilitada || usuarios.contar() > 0) {
                return;
            }
            boolean gerada = senhaInformada == null || senhaInformada.isBlank();
            String senha = gerada ? SenhaProvisoria.gerar() : senhaInformada;
            Usuario.validarSenhaNova(senha);
            gerenciar.cadastrar(new GerenciarUsuariosUseCase.CadastrarComando(nome, login, senha, PerfilUsuario.ADMINISTRADOR));
            if (gerada) {
                LOG.warn("Primeiro acesso criado. Login: {}  Senha: {}  (troque a senha depois de entrar)", login, senha);
            } else {
                LOG.warn("Primeiro acesso criado com o login \"{}\" e a senha definida em ADMIN_SENHA", login);
            }
        };
    }
}
