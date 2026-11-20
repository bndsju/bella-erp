import type { AcaoPermissao, Permissoes } from "@/types/permissao";
import type { PerfilUsuario } from "@/types/usuario";

/**
 * Qual módulo de permissões cada rota da tela pertence. Serve só para esconder o que a pessoa não pode usar: quem
 * de fato barra é a API. Rota que não está aqui (ex.: Ajustes) é livre para qualquer usuário logado.
 */
const MODULO_DA_ROTA: { prefixo: string; modulo: string }[] = [
  { prefixo: "/clientes", modulo: "CLIENTES" },
  { prefixo: "/produtos", modulo: "PRODUTOS" },
  { prefixo: "/categorias", modulo: "PRODUTOS" },
  { prefixo: "/unidades-medida", modulo: "PRODUTOS" },
  { prefixo: "/fornecedores", modulo: "FORNECEDORES" },
  { prefixo: "/transportadoras", modulo: "TRANSPORTADORAS" },
  { prefixo: "/orcamentos", modulo: "ORCAMENTOS" },
  { prefixo: "/pedidos", modulo: "PEDIDOS" },
  { prefixo: "/vendas", modulo: "VENDAS" },
  { prefixo: "/compras", modulo: "COMPRAS" },
  { prefixo: "/estoque", modulo: "ESTOQUE" },
  { prefixo: "/contas-receber", modulo: "CONTAS_RECEBER" },
  { prefixo: "/contas-pagar", modulo: "CONTAS_PAGAR" },
  { prefixo: "/categorias-despesa", modulo: "CONTAS_PAGAR" },
  { prefixo: "/fluxo-caixa", modulo: "FLUXO_CAIXA" },
  { prefixo: "/categorias-fluxo-caixa", modulo: "FLUXO_CAIXA" },
  { prefixo: "/relatorios", modulo: "RELATORIOS" },
  { prefixo: "/auditoria", modulo: "AUDITORIA" },
];

/** Telas que só o administrador abre, sempre (não entram na matriz). */
const SO_ADMINISTRADOR = ["/usuarios", "/permissoes"];

function comecaCom(caminho: string, prefixo: string): boolean {
  return caminho === prefixo || caminho.startsWith(`${prefixo}/`);
}

export function moduloDaRota(caminho: string): string | null {
  if (caminho === "/") return "DASHBOARD";
  // o prefixo mais longo ganha: "/categorias-despesa" não pode cair em "/categorias"
  const achado = MODULO_DA_ROTA.filter((r) => comecaCom(caminho, r.prefixo)).sort(
    (a, b) => b.prefixo.length - a.prefixo.length,
  )[0];
  return achado?.modulo ?? null;
}

export function podeAcessarRota(perfil: PerfilUsuario, permissoes: Permissoes, caminho: string): boolean {
  if (perfil === "ADMINISTRADOR") return true;
  if (SO_ADMINISTRADOR.some((prefixo) => comecaCom(caminho, prefixo))) return false;
  const modulo = moduloDaRota(caminho);
  return modulo === null || (permissoes[modulo]?.includes("VER") ?? false);
}

export function temPermissao(
  perfil: PerfilUsuario,
  permissoes: Permissoes,
  modulo: string,
  acao: AcaoPermissao,
): boolean {
  return perfil === "ADMINISTRADOR" || (permissoes[modulo]?.includes(acao) ?? false);
}

export const ROTULO_PERFIL: Record<PerfilUsuario, string> = {
  ADMINISTRADOR: "Administrador",
  GERENTE: "Gerente",
  FINANCEIRO: "Financeiro",
  VENDEDOR: "Vendedor",
};

export const DESCRICAO_PERFIL: Record<PerfilUsuario, string> = {
  ADMINISTRADOR: "Acesso total, inclusive usuários e permissões. Não pode ser alterado.",
  GERENTE: "Configurável em Permissões. Por padrão, tudo, menos usuários.",
  FINANCEIRO: "Configurável em Permissões. Por padrão, tudo, menos auditoria e usuários.",
  VENDEDOR: "Configurável em Permissões. Por padrão, o dia a dia, sem financeiro nem auditoria.",
};

export const PERFIS: PerfilUsuario[] = ["ADMINISTRADOR", "GERENTE", "FINANCEIRO", "VENDEDOR"];

/** Perfis cujas permissões o administrador pode editar. */
export const PERFIS_EDITAVEIS: PerfilUsuario[] = ["GERENTE", "FINANCEIRO", "VENDEDOR"];
