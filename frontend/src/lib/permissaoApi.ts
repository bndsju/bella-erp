import { api } from "./api";
import type { MatrizPermissoes, PerfilPermissoes, Permissoes } from "@/types/permissao";
import type { PerfilUsuario } from "@/types/usuario";

export const permissaoApi = {
  matriz: () => api.get<MatrizPermissoes>("/permissoes"),
  alterar: (perfil: PerfilUsuario, permissoes: Permissoes) =>
    api.put<PerfilPermissoes>(`/permissoes/${perfil}`, { permissoes }),
  restaurarPadrao: (perfil: PerfilUsuario) =>
    api.post<PerfilPermissoes>(`/permissoes/${perfil}/restaurar-padrao`, {}),
  /** O que o usuário logado pode fazer. */
  minhas: () => api.get<{ permissoes: Permissoes }>("/auth/permissoes"),
};
