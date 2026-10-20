import { api } from "./api";
import type {
  StatusFornecedor,
  FornecedorRequest,
  FornecedorResponse,
} from "@/types/fornecedor";

export const fornecedorApi = {
  listar: (status?: StatusFornecedor) =>
    api.get<FornecedorResponse[]>(`/fornecedores${status ? `?status=${status}` : ""}`),

  buscarPorId: (id: string) => api.get<FornecedorResponse>(`/fornecedores/${id}`),

  cadastrar: (dados: FornecedorRequest) =>
    api.post<FornecedorResponse>("/fornecedores", dados),

  editar: (id: string, dados: FornecedorRequest) =>
    api.put<FornecedorResponse>(`/fornecedores/${id}`, dados),

  alterarStatus: (id: string, status: StatusFornecedor) =>
    api.patch<FornecedorResponse>(`/fornecedores/${id}/status`, { status }),
};
