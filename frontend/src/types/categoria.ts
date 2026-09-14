export type StatusCategoria = "ATIVO" | "INATIVO";

export interface CategoriaResponse {
  id: string;
  nome: string;
  status: StatusCategoria;
  criadoEm: string;
  atualizadoEm: string;
}

export interface CategoriaRequest {
  nome: string;
}
