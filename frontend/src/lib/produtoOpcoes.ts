import type { OpcaoSelecao } from "@/components/ui/selecao-pesquisavel";
import type { ProdutoResponse } from "@/types/produto";

/** Opções para a seleção pesquisável de produtos: acha por código, nome ou código de barras. */
export function opcoesDeProdutos(produtos: ProdutoResponse[]): OpcaoSelecao[] {
  return produtos.map((produto) => ({
    valor: produto.id,
    rotulo: `${produto.codigoInterno} — ${produto.nome}`,
    termos: produto.codigoBarras ?? "",
  }));
}
