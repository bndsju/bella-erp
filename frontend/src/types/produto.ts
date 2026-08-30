import type { CategoriaResponse } from "./categoria";
import type { UnidadeMedidaResponse } from "./unidadeMedida";

export type StatusProduto = "ATIVO" | "INATIVO";

export interface ProdutoResponse {
  id: string;
  codigoInterno: string;
  codigoBarras: string | null;
  nome: string;
  descricao: string | null;
  categoria: CategoriaResponse;
  unidadeMedida: UnidadeMedidaResponse;
  precoCusto: number;
  precoVenda: number;
  estoqueMinimo: number;
  status: StatusProduto;
  criadoEm: string;
  atualizadoEm: string;
}

export interface ProdutoRequest {
  codigoInterno: string;
  codigoBarras?: string;
  nome: string;
  descricao?: string;
  categoriaId: string;
  unidadeMedidaId: string;
  precoCusto: number;
  precoVenda: number;
  estoqueMinimo: number;
}

export interface ProdutoEdicaoRequest {
  codigoBarras?: string;
  nome: string;
  descricao?: string;
  categoriaId: string;
  unidadeMedidaId: string;
  precoCusto: number;
  precoVenda: number;
  estoqueMinimo: number;
}

export interface ListarProdutosParams {
  busca?: string;
  status?: StatusProduto;
  categoriaId?: string;
}
