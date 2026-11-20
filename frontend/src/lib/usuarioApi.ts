import { api } from "./api";
import type {
  CadastrarUsuarioRequest,
  EditarUsuarioRequest,
  StatusUsuario,
  UsuarioResponse,
} from "@/types/usuario";

export const usuarioApi = {
  listar: (status?: StatusUsuario) => api.get<UsuarioResponse[]>(`/usuarios${status ? `?status=${status}` : ""}`),
  cadastrar: (dados: CadastrarUsuarioRequest) => api.post<UsuarioResponse>("/usuarios", dados),
  editar: (id: string, dados: EditarUsuarioRequest) => api.put<UsuarioResponse>(`/usuarios/${id}`, dados),
  alterarStatus: (id: string, status: StatusUsuario) =>
    api.patch<UsuarioResponse>(`/usuarios/${id}/status`, { status }),
  gerarSenhaProvisoria: (id: string) =>
    api.post<{ senhaProvisoria: string }>(`/usuarios/${id}/senha-provisoria`, {}),
};
