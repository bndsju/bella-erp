import type { AcaoPermissao, ModuloPermissao, Permissoes } from "@/types/permissao";

const ORDEM: AcaoPermissao[] = ["VER", "CRIAR", "EDITAR", "CANCELAR"];

function ordenar(acoes: AcaoPermissao[]): AcaoPermissao[] {
  return ORDEM.filter((a) => acoes.includes(a));
}

/**
 * Liga ou desliga uma ação. Criar, editar e cancelar levam o "ver" junto, e tirar o "ver" tira todo o resto:
 * é a mesma regra que o servidor aplica, assim a tela mostra exatamente o que será salvo.
 */
export function alternarAcao(
  permissoes: Permissoes,
  modulo: ModuloPermissao,
  acao: AcaoPermissao,
  marcada: boolean,
): Permissoes {
  const atuais = permissoes[modulo.chave] ?? [];
  let novas: AcaoPermissao[];
  if (marcada) {
    novas = acao === "VER" ? [...atuais, "VER"] : [...atuais, acao, "VER"];
  } else {
    novas = acao === "VER" ? [] : atuais.filter((a) => a !== acao);
  }
  return definirModulo(permissoes, modulo.chave, ordenar([...new Set(novas)]));
}

/** Marca ou desmarca todas as ações que o módulo tem. */
export function alternarTudo(permissoes: Permissoes, modulo: ModuloPermissao, marcar: boolean): Permissoes {
  return definirModulo(permissoes, modulo.chave, marcar ? [...modulo.acoes] : []);
}

function definirModulo(permissoes: Permissoes, chave: string, acoes: AcaoPermissao[]): Permissoes {
  const proximas = { ...permissoes };
  if (acoes.length === 0) {
    delete proximas[chave];
  } else {
    proximas[chave] = acoes;
  }
  return proximas;
}

export function temTudo(permissoes: Permissoes, modulo: ModuloPermissao): boolean {
  const atuais = permissoes[modulo.chave] ?? [];
  return modulo.acoes.every((a) => atuais.includes(a));
}

/** Comparação ignorando a ordem, para saber se há alteração a salvar. */
export function iguais(a: Permissoes, b: Permissoes): boolean {
  const chaves = new Set([...Object.keys(a), ...Object.keys(b)]);
  for (const chave of chaves) {
    const x = [...(a[chave] ?? [])].sort().join(",");
    const y = [...(b[chave] ?? [])].sort().join(",");
    if (x !== y) return false;
  }
  return true;
}

/** Quem trabalha em um módulo precisa enxergar os cadastros que os formulários dele listam. */
const DEPENDENCIAS: { modulo: string; precisaVer: string[] }[] = [
  { modulo: "ORCAMENTOS", precisaVer: ["CLIENTES", "PRODUTOS"] },
  { modulo: "PEDIDOS", precisaVer: ["CLIENTES", "PRODUTOS"] },
  { modulo: "VENDAS", precisaVer: ["CLIENTES", "PRODUTOS"] },
  { modulo: "COMPRAS", precisaVer: ["FORNECEDORES", "PRODUTOS"] },
  { modulo: "ESTOQUE", precisaVer: ["PRODUTOS"] },
  { modulo: "CONTAS_RECEBER", precisaVer: ["CLIENTES"] },
  { modulo: "CONTAS_PAGAR", precisaVer: ["FORNECEDORES"] },
];

/** Avisos (não bloqueiam): o perfil cria ou edita em um módulo, mas não vê algo que a tela dele precisa listar. */
export function avisosDeDependencia(permissoes: Permissoes, modulos: ModuloPermissao[]): string[] {
  const rotulo = (chave: string) => modulos.find((m) => m.chave === chave)?.rotulo ?? chave;
  const avisos: string[] = [];
  for (const { modulo, precisaVer } of DEPENDENCIAS) {
    const acoes = permissoes[modulo] ?? [];
    const trabalha = acoes.includes("CRIAR") || acoes.includes("EDITAR");
    if (!trabalha) continue;
    const faltando = precisaVer.filter((chave) => !(permissoes[chave] ?? []).includes("VER"));
    if (faltando.length > 0) {
      avisos.push(
        `Pode criar ou editar em "${rotulo(modulo)}", mas não vê ${faltando.map((c) => `"${rotulo(c)}"`).join(" e ")}: ` +
          "os formulários não conseguirão listar essas opções.",
      );
    }
  }
  return avisos;
}
