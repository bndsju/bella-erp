import { useMemo } from "react";
import { Badge } from "@/components/ui/badge";
import type { OpcaoSelecao } from "@/components/ui/selecao-pesquisavel";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { BotaoLimpar, CampoMarcar, CampoPeriodo, CampoPesquisa, CampoSelecao, CampoTamanho } from "@/components/relatorios/filtros";
import { TODOS, descrever, periodoObrigatorio, rotuloDe, sem, useFiltros, useLista, type Opcao } from "@/components/relatorios/filtrosBase";
import { Indicador, SemRegistros } from "@/components/relatorios/Indicador";
import { Paginacao } from "@/components/relatorios/Paginacao";
import { SecaoRelatorio } from "@/components/relatorios/SecaoRelatorio";
import { categoriaApi } from "@/lib/categoriaApi";
import { ROTULO_ORIGEM, ROTULO_TIPO, formatarQuantidade, quantidadeComSinal } from "@/lib/estoqueFormatos";
import { textoDoPeriodo, todasAsPaginas } from "@/lib/pdf";
import { produtoApi } from "@/lib/produtoApi";
import { opcoesDeProdutos } from "@/lib/produtoOpcoes";
import { relatorioApi } from "@/lib/relatorioApi";
import { formatarDataHora } from "@/lib/relatorioRotulos";
import type { TipoMovimentacaoEstoque } from "@/types/estoque";

interface Props {
  inicio: string;
  fim: string;
}

interface Cadastros {
  produtos: OpcaoSelecao[];
  categorias: Opcao[];
}

const TIPOS: Opcao[] = (Object.keys(ROTULO_TIPO) as TipoMovimentacaoEstoque[]).map((valor) => ({
  valor,
  rotulo: ROTULO_TIPO[valor],
}));

function SecaoPosicao({ produtos, categorias }: Cadastros) {
  const { filtros: f, alterado, mudar, irParaPagina, limpar } = useFiltros({
    produtoId: TODOS,
    categoriaId: TODOS,
    apenasAbaixo: false,
    pagina: 0,
    tamanho: 10,
  });
  const params = {
    produtoId: sem(f.produtoId),
    categoriaId: sem(f.categoriaId),
    apenasAbaixoDoMinimo: f.apenasAbaixo,
  };

  return (
    <SecaoRelatorio
      titulo="Posição do estoque"
      chave={JSON.stringify(f)}
      buscar={() => relatorioApi.estoquePosicao({ ...params, pagina: f.pagina, tamanho: f.tamanho })}
      exportar={async () => {
        const { primeira, itens } = await todasAsPaginas(
          (p, t) => relatorioApi.estoquePosicao({ ...params, pagina: p, tamanho: t }),
          (resposta) => resposta.produtos,
        );
        return {
          titulo: "Posição do estoque",
          nomeArquivo: "posicao-do-estoque",
          filtros: descrever([
            ["Produto", rotuloDe(produtos, f.produtoId)],
            ["Categoria", rotuloDe(categorias, f.categoriaId)],
            ["Somente", f.apenasAbaixo ? "abaixo do mínimo" : undefined],
          ]),
          resumo: [
            ["Produtos", String(primeira.totalProdutos)],
            ["Abaixo do mínimo", String(primeira.produtosAbaixoDoMinimo)],
          ],
          tabelas: [
            {
              colunas: ["Produto", "Categoria", "Físico", "Reservado", "Disponível", "Mínimo", "Situação"],
              linhas: itens.map((produto) => [
                `${produto.codigoInterno} — ${produto.nome}`,
                produto.categoriaNome ?? "—",
                formatarQuantidade(produto.estoqueFisico),
                formatarQuantidade(produto.quantidadeReservada),
                formatarQuantidade(produto.quantidadeDisponivel),
                formatarQuantidade(produto.estoqueMinimo),
                produto.abaixoDoMinimo ? "Repor" : "Em dia",
              ]),
              direita: [2, 3, 4, 5],
            },
          ],
        };
      }}
      filtros={
        <>
          <CampoPesquisa
            id="posicao-produto"
            rotulo="Produto"
            valor={f.produtoId}
            aoMudar={(produtoId) => mudar({ produtoId })}
            opcoes={produtos}
            rotuloTodos="Todos os produtos"
            placeholder="Buscar por código ou nome"
          />
          <CampoSelecao
            id="posicao-categoria"
            rotulo="Categoria"
            valor={f.categoriaId}
            aoMudar={(categoriaId) => mudar({ categoriaId })}
            opcoes={categorias}
            rotuloTodos="Todas"
          />
          <CampoMarcar
            rotulo="Só abaixo do mínimo"
            marcado={f.apenasAbaixo}
            aoMudar={(apenasAbaixo) => mudar({ apenasAbaixo })}
          />
          <CampoTamanho id="posicao-tamanho" valor={f.tamanho} aoMudar={(tamanho) => mudar({ tamanho })} />
          <BotaoLimpar visivel={alterado} aoClicar={limpar} />
        </>
      }
    >
      {(dados) => (
        <>
          <div className="grade grade--2">
            <Indicador rotulo="Produtos" valor={String(dados.totalProdutos)} />
            <Indicador rotulo="Abaixo do mínimo" valor={String(dados.produtosAbaixoDoMinimo)} />
          </div>
          {dados.produtos.itens.length === 0 ? (
            <SemRegistros texto="Nenhum produto com esses filtros." />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Produto</TableHead>
                  <TableHead>Categoria</TableHead>
                  <TableHead className="text-right">Físico</TableHead>
                  <TableHead className="text-right">Reservado</TableHead>
                  <TableHead className="text-right">Disponível</TableHead>
                  <TableHead className="text-right">Mínimo</TableHead>
                  <TableHead>Situação</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.produtos.itens.map((produto) => (
                  <TableRow key={produto.produtoId}>
                    <TableCell className="tabela__forte">
                      {produto.codigoInterno} — {produto.nome}
                    </TableCell>
                    <TableCell>{produto.categoriaNome ?? "—"}</TableCell>
                    <TableCell className="text-right">{formatarQuantidade(produto.estoqueFisico)}</TableCell>
                    <TableCell className="text-right">{formatarQuantidade(produto.quantidadeReservada)}</TableCell>
                    <TableCell className="text-right">{formatarQuantidade(produto.quantidadeDisponivel)}</TableCell>
                    <TableCell className="text-right">{formatarQuantidade(produto.estoqueMinimo)}</TableCell>
                    <TableCell>
                      {produto.abaixoDoMinimo ? (
                        <Badge variant="destructive">Repor</Badge>
                      ) : (
                        <Badge variant="default">Em dia</Badge>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <Paginacao pagina={dados.produtos} onMudar={irParaPagina} />
        </>
      )}
    </SecaoRelatorio>
  );
}

function SecaoMovimentacoes({ inicio, fim, produtos }: Props & Pick<Cadastros, "produtos">) {
  const { filtros: f, alterado, mudar, irParaPagina, limpar } = useFiltros({
    inicio,
    fim,
    produtoId: TODOS,
    tipo: TODOS,
    pagina: 0,
    tamanho: 10,
  });
  const params = { inicio: f.inicio, fim: f.fim, produtoId: sem(f.produtoId), tipo: sem(f.tipo) };

  return (
    <SecaoRelatorio
      titulo="Movimentações de estoque"
      chave={JSON.stringify(f)}
      bloqueio={periodoObrigatorio(f.inicio, f.fim)}
      buscar={() => relatorioApi.estoqueMovimentacoes({ ...params, pagina: f.pagina, tamanho: f.tamanho })}
      exportar={async () => {
        const { primeira, itens } = await todasAsPaginas(
          (p, t) => relatorioApi.estoqueMovimentacoes({ ...params, pagina: p, tamanho: t }),
          (resposta) => resposta.movimentacoes,
        );
        return {
          titulo: "Movimentações de estoque",
          nomeArquivo: `movimentacoes-estoque-${f.inicio}-a-${f.fim}`,
          periodo: textoDoPeriodo(f.inicio, f.fim),
          filtros: descrever([
            ["Produto", rotuloDe(produtos, f.produtoId)],
            ["Tipo", rotuloDe(TIPOS, f.tipo)],
          ]),
          resumo: primeira.totaisPorTipo.map((total): [string, string] => [
            ROTULO_TIPO[total.tipo],
            `${formatarQuantidade(total.quantidadeTotal)} (${total.quantidadeMovimentacoes} mov.)`,
          ]),
          tabelas: [
            {
              colunas: ["Data", "Produto", "Tipo", "Quantidade", "Origem", "Responsável"],
              linhas: itens.map((mov) => [
                formatarDataHora(mov.dataHora),
                mov.produtoNome,
                ROTULO_TIPO[mov.tipo],
                quantidadeComSinal(mov.tipo, mov.quantidade),
                ROTULO_ORIGEM[mov.origem],
                mov.usuarioResponsavel ?? "—",
              ]),
              direita: [3],
            },
          ],
        };
      }}
      filtros={
        <>
          <CampoPeriodo prefixo="mov-estoque" inicio={f.inicio} fim={f.fim} aoMudar={mudar} />
          <CampoPesquisa
            id="mov-estoque-produto"
            rotulo="Produto"
            valor={f.produtoId}
            aoMudar={(produtoId) => mudar({ produtoId })}
            opcoes={produtos}
            rotuloTodos="Todos os produtos"
            placeholder="Buscar por código ou nome"
          />
          <CampoSelecao
            id="mov-estoque-tipo"
            rotulo="Tipo"
            valor={f.tipo}
            aoMudar={(tipo) => mudar({ tipo })}
            opcoes={TIPOS}
            rotuloTodos="Todos"
          />
          <CampoTamanho id="mov-estoque-tamanho" valor={f.tamanho} aoMudar={(tamanho) => mudar({ tamanho })} />
          <BotaoLimpar visivel={alterado} aoClicar={limpar} />
        </>
      }
    >
      {(dados) => (
        <>
          {dados.totaisPorTipo.length > 0 && (
            <div className="grade grade--3">
              {dados.totaisPorTipo.map((total) => (
                <Indicador
                  key={total.tipo}
                  rotulo={ROTULO_TIPO[total.tipo]}
                  valor={formatarQuantidade(total.quantidadeTotal)}
                  nota={`${total.quantidadeMovimentacoes} movimentação(ões)`}
                />
              ))}
            </div>
          )}
          {dados.movimentacoes.itens.length === 0 ? (
            <SemRegistros texto="Nenhuma movimentação com esses filtros." />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Data</TableHead>
                  <TableHead>Produto</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead className="text-right">Quantidade</TableHead>
                  <TableHead>Origem</TableHead>
                  <TableHead>Responsável</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.movimentacoes.itens.map((mov) => (
                  <TableRow key={mov.id}>
                    <TableCell>{formatarDataHora(mov.dataHora)}</TableCell>
                    <TableCell className="tabela__forte">{mov.produtoNome}</TableCell>
                    <TableCell>{ROTULO_TIPO[mov.tipo]}</TableCell>
                    <TableCell className="text-right">{quantidadeComSinal(mov.tipo, mov.quantidade)}</TableCell>
                    <TableCell>{ROTULO_ORIGEM[mov.origem]}</TableCell>
                    <TableCell>{mov.usuarioResponsavel ?? "—"}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <Paginacao pagina={dados.movimentacoes} onMudar={irParaPagina} />
        </>
      )}
    </SecaoRelatorio>
  );
}

export function RelatoriosEstoque({ inicio, fim }: Props) {
  const produtosBrutos = useLista(() => produtoApi.listar());
  const categoriasBrutas = useLista(() => categoriaApi.listar());
  const produtos = useMemo(() => opcoesDeProdutos(produtosBrutos), [produtosBrutos]);
  const categorias = useMemo<Opcao[]>(
    () => categoriasBrutas.map((categoria) => ({ valor: categoria.id, rotulo: categoria.nome })),
    [categoriasBrutas],
  );

  return (
    <>
      <SecaoPosicao produtos={produtos} categorias={categorias} />
      <SecaoMovimentacoes inicio={inicio} fim={fim} produtos={produtos} />
    </>
  );
}
