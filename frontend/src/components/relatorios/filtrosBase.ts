import { useEffect, useRef, useState } from "react";
import type { OpcaoSelecao } from "@/components/ui/selecao-pesquisavel";

/** Valor da opção "sem filtro" nas seleções. */
export const TODOS = "TODOS";

export interface Opcao {
  valor: string;
  rotulo: string;
}

/** Valor para a consulta: "TODOS" e vazio viram "sem filtro". */
export function sem(valor: string): string | undefined {
  return valor === TODOS || valor === "" ? undefined : valor;
}

export function rotuloDe(opcoes: Opcao[] | OpcaoSelecao[], valor: string): string | undefined {
  return opcoes.find((opcao) => opcao.valor === valor)?.rotulo;
}

/** Lista [rótulo, valor] -> "Rótulo: valor", só dos filtros que estão ativos (para o cabeçalho do PDF). */
export function descrever(itens: [string, string | undefined][]): string[] {
  return itens.filter(([, valor]) => Boolean(valor)).map(([rotulo, valor]) => `${rotulo}: ${valor}`);
}

export function periodoInvalido(inicio: string, fim: string): string | null {
  return inicio && fim && fim < inicio ? "A data final não pode ser anterior à data inicial." : null;
}

/** Relatórios de atividade: o servidor assumiria "mês corrente" sem datas, o que enganaria; exigimos as duas. */
export function periodoObrigatorio(inicio: string, fim: string): string | null {
  if (!inicio || !fim) return "Informe a data inicial e a data final.";
  return periodoInvalido(inicio, fim);
}

/** Carrega uma lista de cadastro uma vez (para as opções dos filtros); se falhar, fica vazia. */
export function useLista<T>(carregar: () => Promise<T[]>): T[] {
  const [lista, setLista] = useState<T[]>([]);
  const carregarRef = useRef(carregar);
  useEffect(() => {
    let ativo = true;
    carregarRef
      .current()
      .then((resultado) => {
        if (ativo) setLista(resultado);
      })
      .catch(() => {});
    return () => {
      ativo = false;
    };
  }, []);
  return lista;
}

/** Estado dos filtros de um relatório: qualquer mudança volta para a primeira página. */
export function useFiltros<T extends { pagina: number }>(padrao: T) {
  const [inicial] = useState<T>(padrao);
  const [filtros, setFiltros] = useState<T>(padrao);
  const alterado = JSON.stringify({ ...filtros, pagina: 0 }) !== JSON.stringify({ ...inicial, pagina: 0 });
  return {
    filtros,
    alterado,
    mudar: (parte: Partial<T>) => setFiltros((atual) => ({ ...atual, pagina: 0, ...parte })),
    irParaPagina: (pagina: number) => setFiltros((atual) => ({ ...atual, pagina })),
    limpar: () => setFiltros(inicial),
  };
}
