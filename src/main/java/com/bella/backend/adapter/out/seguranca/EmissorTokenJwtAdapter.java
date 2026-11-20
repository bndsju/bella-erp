package com.bella.backend.adapter.out.seguranca;

import com.bella.backend.domain.usuario.model.Usuario;
import com.bella.backend.domain.usuario.port.out.EmissorTokenPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/** Token JWT assinado (HS256). O token só carrega o id do usuário; perfil e status são lidos do banco a cada requisição. */
@Component
public class EmissorTokenJwtAdapter implements EmissorTokenPort {

    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    private final SecretKey chave;
    private final Duration validade;

    public EmissorTokenJwtAdapter(@Value("${bella.seguranca.jwt-segredo:}") String segredo,
                                  @Value("${bella.seguranca.jwt-validade-horas:8}") long validadeHoras) {
        if (segredo == null || segredo.isBlank()) {
            // Sem JWT_SEGREDO o segredo é aleatório a cada partida: nada fica inseguro, mas os logins caem ao reiniciar.
            byte[] sorteado = new byte[48];
            new SecureRandom().nextBytes(sorteado);
            segredo = Base64.getEncoder().encodeToString(sorteado);
            LoggerFactory.getLogger(EmissorTokenJwtAdapter.class)
                    .warn("JWT_SEGREDO não definido: usando um segredo temporário. Defina a variável para manter os logins ao reiniciar.");
        }
        if (segredo.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "bella.seguranca.jwt-segredo precisa ter pelo menos " + TAMANHO_MINIMO_SEGREDO + " caracteres");
        }
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.validade = Duration.ofHours(validadeHoras);
    }

    @Override
    public TokenEmitido emitir(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(validade);
        String token = Jwts.builder()
                .subject(usuario.getId().toString())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiraEm))
                .signWith(chave)
                .compact();
        return new TokenEmitido(token, expiraEm);
    }

    @Override
    public Optional<DadosToken> validar(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
            return Optional.of(new DadosToken(UUID.fromString(claims.getSubject()), claims.getIssuedAt().toInstant()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
