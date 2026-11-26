package com.bella.backend.adapter.in.web.seguranca;

import com.bella.backend.application.auditoria.EventosSeguranca;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Freia robôs e tentativas de adivinhar senha: limita quantas vezes cada endereço IP pode chamar o login e o
 * "esqueci a senha" numa janela de tempo. Funciona em memória (some ao reiniciar), o que basta para uma só instância.
 * O bloqueio por login, mais estrito, continua existindo no serviço de autenticação.
 */
public class LimitadorDeRequisicoesFiltro extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(LimitadorDeRequisicoesFiltro.class);
    private static final int LIMPEZA_A_CADA = 500;

    public record Regra(String caminho, int maximo) {
    }

    private final Map<String, Regra> regras = new ConcurrentHashMap<>();
    private final Map<String, Janela> janelas = new ConcurrentHashMap<>();
    private final Duration duracao;
    private final EventosSeguranca eventos;
    private final ObjectMapper mapper;
    private int contador;

    public LimitadorDeRequisicoesFiltro(Duration duracao, EventosSeguranca eventos, ObjectMapper mapper, Regra... regras) {
        this.duracao = duracao;
        this.eventos = eventos;
        this.mapper = mapper;
        for (Regra regra : regras) {
            this.regras.put(regra.caminho(), regra);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !regras.containsKey(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Regra regra = regras.get(request.getRequestURI());
        String ip = request.getRemoteAddr();
        Instant agora = Instant.now();
        limparDeVezEmQuando(agora);

        Janela janela = janelas.compute(regra.caminho() + "|" + ip, (chave, atual) -> {
            Janela j = atual == null || !agora.isBefore(atual.fim) ? new Janela(agora.plus(duracao)) : atual;
            j.pedidos++;
            return j;
        });

        if (janela.pedidos <= regra.maximo()) {
            chain.doFilter(request, response);
            return;
        }

        if (janela.pedidos == regra.maximo() + 1) { // registra uma vez por janela, para não encher a auditoria
            LOG.warn("Limite de requisições excedido em {} pelo endereço {}", regra.caminho(), ip);
            try {
                eventos.limiteExcedido(regra.caminho(), ip);
            } catch (RuntimeException ex) {
                LOG.error("Não foi possível registrar o limite excedido na auditoria", ex);
            }
        }
        long segundos = Math.max(1, Duration.between(agora, janela.fim).toSeconds());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(segundos));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("timestamp", agora.toString());
        corpo.put("status", 429);
        corpo.put("mensagem", "Muitas tentativas. Aguarde alguns minutos e tente de novo.");
        mapper.writeValue(response.getWriter(), corpo);
    }

    private synchronized void limparDeVezEmQuando(Instant agora) {
        if (++contador % LIMPEZA_A_CADA == 0) {
            janelas.values().removeIf(j -> !agora.isBefore(j.fim));
        }
    }

    private static final class Janela {
        final Instant fim;
        int pedidos;

        Janela(Instant fim) {
            this.fim = fim;
        }
    }
}
