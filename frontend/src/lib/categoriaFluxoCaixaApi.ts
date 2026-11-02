import { api } from "./api";
import type { CategoriaFluxoCaixaRequest, CategoriaFluxoCaixaResponse, StatusCategoriaFluxoCaixa } from "@/types/categoriaFluxoCaixa";

export const categoriaFluxoCaixaApi = {
  listar: (status?: StatusCategoriaFluxoCaixa) =>
    api.get<CategoriaFluxoCaixaResponse[]>(`/categorias-fluxo-caixa${status ? `?status=${status}` : ""}`),

  buscarPorId: (id: string) => api.get<CategoriaFluxoCaixaResponse>(`/categorias-fluxo-caixa/${id}`),

  cadastrar: (dados: CategoriaFluxoCaixaRequest) => api.post<CategoriaFluxoCaixaResponse>("/categorias-fluxo-caixa", dados),

  editar: (id: string, dados: CategoriaFluxoCaixaRequest) =>
    api.put<CategoriaFluxoCaixaResponse>(`/categorias-fluxo-caixa/${id}`, dados),

  alterarStatus: (id: string, status: StatusCategoriaFluxoCaixa) =>
    api.patch<CategoriaFluxoCaixaResponse>(`/categorias-fluxo-caixa/${id}/status`, { status }),
};
