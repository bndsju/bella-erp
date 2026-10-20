export type StatusFornecedor = "ATIVO" | "INATIVO";

export interface FornecedorResponse {
  id: string;
  nome: string;
  telefone: string | null;
  status: StatusFornecedor;
  criadoEm: string;
  atualizadoEm: string;
}

export interface FornecedorRequest {
  nome: string;
  telefone?: string;
}
