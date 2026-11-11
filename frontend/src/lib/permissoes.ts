import type { PerfilUsuario } from "@/types/usuario";

/**
 * Quem pode abrir cada área. Espelha as regras do servidor (SegurancaConfig): aqui serve só para esconder o que
 * a pessoa não pode usar; quem de fato barra é a API. Rotas não listadas são livres para qualquer usuário logado.
 */
const REGRAS: { prefixo: string; perfis: PerfilUsuario[] }[] = [
  { prefixo: "/usuarios", perfis: ["ADMINISTRADOR"] },
  { prefixo: "/auditoria", perfis: ["ADMINISTRADOR", "GERENTE"] },
  ...[
    "/contas-receber",
    "/contas-pagar",
    "/fluxo-caixa",
    "/categorias-despesa",
    "/categorias-fluxo-caixa",
    "/relatorios",
  ].map((prefixo) => ({ prefixo, perfis: ["ADMINISTRADOR", "GERENTE", "FINANCEIRO"] as PerfilUsuario[] })),
];

export function podeAcessar(perfil: PerfilUsuario, caminho: string): boolean {
  const regra = REGRAS.find((r) => caminho === r.prefixo || caminho.startsWith(`${r.prefixo}/`));
  return !regra || regra.perfis.includes(perfil);
}

export const ROTULO_PERFIL: Record<PerfilUsuario, string> = {
  ADMINISTRADOR: "Administrador",
  GERENTE: "Gerente",
  FINANCEIRO: "Financeiro",
  VENDEDOR: "Vendedor",
};

export const DESCRICAO_PERFIL: Record<PerfilUsuario, string> = {
  ADMINISTRADOR: "Acesso total, inclusive usuários e auditoria",
  GERENTE: "Tudo, exceto gerenciar usuários",
  FINANCEIRO: "Operação e área financeira e relatórios, sem auditoria",
  VENDEDOR: "Operação (clientes, produtos, vendas, estoque), sem financeiro",
};

export const PERFIS: PerfilUsuario[] = ["ADMINISTRADOR", "GERENTE", "FINANCEIRO", "VENDEDOR"];
