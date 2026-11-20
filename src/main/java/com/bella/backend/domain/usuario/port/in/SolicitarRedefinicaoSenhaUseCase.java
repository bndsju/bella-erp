package com.bella.backend.domain.usuario.port.in;

public interface SolicitarRedefinicaoSenhaUseCase {

    /**
     * Registra o pedido "esqueci a senha". Não informa se o login existe: login desconhecido ou inativo
     * é ignorado em silêncio, para a tela não servir de lista de usuários.
     */
    void solicitar(String login);
}
