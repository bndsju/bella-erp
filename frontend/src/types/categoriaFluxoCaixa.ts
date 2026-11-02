export type StatusCategoriaFluxoCaixa = "ATIVO" | "INATIVO";

export interface CategoriaFluxoCaixaResponse {
  id: string;
  nome: string;
  status: StatusCategoriaFluxoCaixa;
  criadoEm: string;
  atualizadoEm: string;
}

export interface CategoriaFluxoCaixaRequest {
  nome: string;
}
