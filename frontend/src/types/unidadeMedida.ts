export type StatusUnidadeMedida = "ATIVO" | "INATIVO";

export interface UnidadeMedidaResponse {
  id: string;
  nome: string;
  sigla: string;
  status: StatusUnidadeMedida;
  criadoEm: string;
  atualizadoEm: string;
}

export interface UnidadeMedidaRequest {
  nome: string;
  sigla: string;
}
