package com.bella.backend.domain.usuario.port.out;

public interface CodificadorSenhaPort {

    String codificar(String senha);

    boolean confere(String senha, String hash);
}
