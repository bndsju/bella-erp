package com.bella.backend.config;

import com.bella.backend.adapter.in.web.seguranca.AutorizadorPorPermissao;
import com.bella.backend.adapter.in.web.seguranca.FiltroAutenticacaoJwt;
import com.bella.backend.adapter.in.web.seguranca.LimitadorDeRequisicoesFiltro;
import com.bella.backend.application.auditoria.EventosSeguranca;
import com.bella.backend.domain.permissao.port.in.ConsultarPermissoesUseCase;
import com.bella.backend.domain.usuario.port.out.EmissorTokenPort;
import com.bella.backend.domain.usuario.port.out.UsuarioRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Segurança da API: sem sessão, autenticação por token JWT (Bearer) e acesso por permissões configuráveis do perfil.
 * <p>
 * Com {@code bella.seguranca.habilitada=false} tudo fica liberado; isso existe só para os testes de integração dos
 * demais módulos e nunca deve ser usado fora deles.
 */
@Configuration
public class SegurancaConfig {

    private static final String ADMINISTRADOR = "ADMINISTRADOR";

    @Bean
    SecurityFilterChain filtroDeSeguranca(HttpSecurity http, EmissorTokenPort emissorTokenPort,
                                          UsuarioRepositoryPort usuarioRepositoryPort, ConsultarPermissoesUseCase permissoes,
                                          ObjectMapper objectMapper, EventosSeguranca eventosSeguranca,
                                          @Value("${bella.seguranca.habilitada:true}") boolean habilitada,
                                          @Value("${bella.seguranca.limite.janela-minutos:15}") long janelaMinutos,
                                          @Value("${bella.seguranca.limite.login-por-ip:30}") int loginPorIp,
                                          @Value("${bella.seguranca.limite.esqueci-senha-por-ip:5}") int esqueciSenhaPorIp) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable);

        if (!habilitada) {
            http.authorizeHttpRequests(a -> a.anyRequest().permitAll());
            return http.build();
        }

        http.addFilterBefore(new LimitadorDeRequisicoesFiltro(Duration.ofMinutes(janelaMinutos), eventosSeguranca, objectMapper,
                                new LimitadorDeRequisicoesFiltro.Regra("/api/auth/login", loginPorIp),
                                new LimitadorDeRequisicoesFiltro.Regra("/api/auth/esqueci-senha", esqueciSenhaPorIp)),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new FiltroAutenticacaoJwt(emissorTokenPort, usuarioRepositoryPort),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                responder(res, objectMapper, HttpStatus.UNAUTHORIZED, "Sessão expirada ou não autenticada"))
                        .accessDeniedHandler((req, res, ex) -> {
                            var atual = SecurityContextHolder.getContext().getAuthentication();
                            boolean provisoria = atual != null && atual.getAuthorities().stream()
                                    .anyMatch(a -> FiltroAutenticacaoJwt.PRECISA_TROCAR_SENHA.equals(a.getAuthority()));
                            responder(res, objectMapper, HttpStatus.FORBIDDEN, provisoria
                                    ? "Defina uma nova senha para continuar"
                                    : "Seu perfil não tem acesso a este recurso");
                        }))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/esqueci-senha").permitAll()
                        .requestMatchers("/api/auth/**").authenticated()
                        // usuários e permissões: sempre e só o administrador, não são configuráveis
                        .requestMatchers("/api/usuarios/**", "/api/permissoes/**").hasRole(ADMINISTRADOR)
                        // o resto, pela matriz de permissões do perfil (módulo + ação)
                        .anyRequest().access(new AutorizadorPorPermissao(permissoes)));
        return http.build();
    }

    private static void responder(HttpServletResponse res, ObjectMapper mapper, HttpStatus status, String mensagem)
            throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", Instant.now().toString());
        corpo.put("status", status.value());
        corpo.put("mensagem", mensagem);
        mapper.writeValue(res.getWriter(), corpo);
    }
}
