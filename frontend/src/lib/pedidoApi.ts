import { api } from "./api";
import type {
  CriarPedidoDeOrcamentoRequest,
  ListarPedidosParams,
  PedidoRequest,
  PedidoResponse,
} from "@/types/pedido";

function queryString(params: ListarPedidosParams): string {
  const query = new URLSearchParams();
  if (params.clienteId) query.set("clienteId", params.clienteId);
  if (params.status) query.set("status", params.status);
  const texto = query.toString();
  return texto ? `?${texto}` : "";
}

export const pedidoApi = {
  listar: (params: ListarPedidosParams = {}) => api.get<PedidoResponse[]>(`/pedidos${queryString(params)}`),

  buscarPorId: (id: string) => api.get<PedidoResponse>(`/pedidos/${id}`),

  cadastrar: (dados: PedidoRequest) => api.post<PedidoResponse>("/pedidos", dados),

  criarAPartirDeOrcamento: (dados: CriarPedidoDeOrcamentoRequest) =>
    api.post<PedidoResponse>("/pedidos/a-partir-de-orcamento", dados),

  editar: (id: string, dados: PedidoRequest) => api.put<PedidoResponse>(`/pedidos/${id}`, dados),

  confirmar: (id: string) => api.patch<PedidoResponse>(`/pedidos/${id}/confirmar`, {}),

  iniciarSeparacao: (id: string) => api.patch<PedidoResponse>(`/pedidos/${id}/iniciar-separacao`, {}),

  marcarProntoParaEntrega: (id: string) =>
    api.patch<PedidoResponse>(`/pedidos/${id}/marcar-pronto-para-entrega`, {}),

  entregar: (id: string) => api.patch<PedidoResponse>(`/pedidos/${id}/entregar`, {}),

  cancelar: (id: string) => api.patch<PedidoResponse>(`/pedidos/${id}/cancelar`, {}),
};
