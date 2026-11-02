package com.bella.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Garante que a transação seja aberta antes (por fora) do aspecto de auditoria, que usa a precedência
 * mais baixa. Assim a auditoria roda dentro da transação do serviço: se não puder ser gravada, a
 * operação auditada também é desfeita.
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true, order = Ordered.LOWEST_PRECEDENCE - 100)
public class TransacaoConfig {
}
