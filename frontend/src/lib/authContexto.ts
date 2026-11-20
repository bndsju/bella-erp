import { createContext, useContext } from "react";
import type { AcaoPermissao } from "@/types/permissao";
import type { UsuarioResponse } from "@/types/usuario";

export interface Autenticacao {
  /** Usuário logado; nulo quando não há sessão. */
  usuario: UsuarioResponse | null;
  /** Verificando o token guardado e carregando as permissões ao abrir o sistema. */
  carregando: boolean;
  entrar: (login: string, senha: string) => Promise<void>;
  sair: () => void;
  /** O usuário pode fazer a ação no módulo? Sem a ação informada, basta poder ver. */
  pode: (modulo: string, acao?: AcaoPermissao) => boolean;
  /** O usuário pode abrir esta rota da tela? */
  podeAcessarRota: (caminho: string) => boolean;
}

export const AuthContexto = createContext<Autenticacao | null>(null);

export function useAuth(): Autenticacao {
  const contexto = useContext(AuthContexto);
  if (!contexto) {
    throw new Error("useAuth precisa estar dentro do AuthProvider");
  }
  return contexto;
}
