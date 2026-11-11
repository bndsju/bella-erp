export type PerfilUsuario = "ADMINISTRADOR" | "GERENTE" | "FINANCEIRO" | "VENDEDOR";
export type StatusUsuario = "ATIVO" | "INATIVO";

export interface UsuarioResponse {
  id: string;
  nome: string;
  login: string;
  perfil: PerfilUsuario;
  status: StatusUsuario;
  ultimoAcessoEm: string | null;
  /** Entrou com senha gerada pelo administrador: precisa definir uma nova antes de usar o sistema. */
  senhaProvisoria: boolean;
  /** Quando a pessoa pediu "esqueci a senha" e ainda não foi atendida. */
  redefinicaoSolicitadaEm: string | null;
  criadoEm: string;
}

export interface SessaoResponse {
  token: string;
  expiraEm: string;
  usuario: UsuarioResponse;
}

export interface CadastrarUsuarioRequest {
  nome: string;
  login: string;
  senha: string;
  perfil: PerfilUsuario;
}

export interface EditarUsuarioRequest {
  nome: string;
  perfil: PerfilUsuario;
}
