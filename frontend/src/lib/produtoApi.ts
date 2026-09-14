import { api } from "./api";
import type {
  ListarProdutosParams,
  ProdutoEdicaoRequest,
  ProdutoRequest,
  ProdutoResponse,
  StatusProduto,
} from "@/types/produto";

function queryString(params: ListarProdutosParams): string {
  const query = new URLSearchParams();
  if (params.busca) query.set("busca", params.busca);
  if (params.status) query.set("status", params.status);
  if (params.categoriaId) query.set("categoriaId", params.categoriaId);
  const texto = query.toString();
  return texto ? `?${texto}` : "";
}

export const produtoApi = {
  listar: (params: ListarProdutosParams = {}) =>
    api.get<ProdutoResponse[]>(`/produtos${queryString(params)}`),

  buscarPorId: (id: string) => api.get<ProdutoResponse>(`/produtos/${id}`),

  cadastrar: (dados: ProdutoRequest) => api.post<ProdutoResponse>("/produtos", dados),

  editar: (id: string, dados: ProdutoEdicaoRequest) =>
    api.put<ProdutoResponse>(`/produtos/${id}`, dados),

  alterarStatus: (id: string, status: StatusProduto) =>
    api.patch<ProdutoResponse>(`/produtos/${id}/status`, { status }),
};
