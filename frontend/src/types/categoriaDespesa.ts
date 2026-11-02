export type StatusCategoriaDespesa = "ATIVO" | "INATIVO";

export interface CategoriaDespesaResponse {
  id: string;
  nome: string;
  status: StatusCategoriaDespesa;
  criadoEm: string;
  atualizadoEm: string;
}

export interface CategoriaDespesaRequest {
  nome: string;
}
