export type ModuloAuditoria =
  | "CLIENTE"
  | "PRODUTO"
  | "ORCAMENTO"
  | "PEDIDO"
  | "VENDA"
  | "COMPRA"
  | "ESTOQUE"
  | "CONTA_RECEBER"
  | "CONTA_PAGAR"
  | "USUARIO"
  | "PERMISSAO"
  | "SEGURANCA";

export type OperacaoAuditoria =
  | "CRIACAO"
  | "ALTERACAO"
  | "INATIVACAO"
  | "REATIVACAO"
  | "CANCELAMENTO"
  | "ALTERACAO_STATUS"
  | "REGISTRO_PAGAMENTO"
  | "MOVIMENTACAO_ESTOQUE"
  | "LOGIN_SUCESSO"
  | "LOGIN_FALHA"
  | "LOGIN_BLOQUEADO"
  | "LIMITE_EXCEDIDO"
  | "REDEFINICAO_SOLICITADA"
  | "SENHA_ALTERADA"
  | "SENHA_PROVISORIA"
  | "ALTERACAO_PERMISSOES";

export interface RegistroAuditoria {
  id: string;
  modulo: ModuloAuditoria;
  entidadeId: string;
  operacao: OperacaoAuditoria;
  usuario: string;
  dataHora: string;
  /** Só os campos que mudaram (ou nada, na criação). */
  valoresAnteriores: Record<string, unknown> | null;
  /** Só os campos que mudaram (ou o estado completo, na criação). */
  novosValores: Record<string, unknown> | null;
  motivo: string | null;
}
