import { api } from "./api";
import type {
  ClienteRequest,
  ClienteResponse,
  ListarClientesParams,
  StatusCliente,
} from "@/types/cliente";

function queryString(params: ListarClientesParams): string {
  const query = new URLSearchParams();
  if (params.busca) query.set("busca", params.busca);
  if (params.status) query.set("status", params.status);
  const texto = query.toString();
  return texto ? `?${texto}` : "";
}

export const clienteApi = {
  listar: (params: ListarClientesParams = {}) =>
    api.get<ClienteResponse[]>(`/clientes${queryString(params)}`),

  buscarPorId: (id: string) => api.get<ClienteResponse>(`/clientes/${id}`),

  cadastrar: (dados: ClienteRequest) => api.post<ClienteResponse>("/clientes", dados),

  editar: (id: string, dados: ClienteRequest) =>
    api.put<ClienteResponse>(`/clientes/${id}`, dados),

  alterarStatus: (id: string, status: StatusCliente) =>
    api.patch<ClienteResponse>(`/clientes/${id}/status`, { status }),
};
