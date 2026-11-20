import type { PerfilUsuario } from "./usuario";

export type AcaoPermissao = "VER" | "CRIAR" | "EDITAR" | "CANCELAR";

/** Ações concedidas, por módulo (chave do módulo). Módulo sem entrada = sem nenhuma permissão. */
export type Permissoes = Record<string, AcaoPermissao[]>;

export interface ModuloPermissao {
  chave: string;
  rotulo: string;
  grupo: string;
  /** Só as ações que existem no módulo (ex.: Relatórios só tem "Ver"). */
  acoes: AcaoPermissao[];
}

export interface PerfilPermissoes {
  perfil: PerfilUsuario;
  /** Administrador: sempre tudo, não editável. */
  fixo: boolean;
  permissoes: Permissoes;
}

export interface MatrizPermissoes {
  acoes: { chave: AcaoPermissao; rotulo: string }[];
  modulos: ModuloPermissao[];
  perfis: PerfilPermissoes[];
}
