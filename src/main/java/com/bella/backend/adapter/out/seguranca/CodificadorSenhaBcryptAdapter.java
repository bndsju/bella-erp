package com.bella.backend.adapter.out.seguranca;

import com.bella.backend.domain.usuario.port.out.CodificadorSenhaPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CodificadorSenhaBcryptAdapter implements CodificadorSenhaPort {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    @Override
    public String codificar(String senha) {
        return encoder.encode(senha);
    }

    @Override
    public boolean confere(String senha, String hash) {
        return encoder.matches(senha, hash);
    }
}
