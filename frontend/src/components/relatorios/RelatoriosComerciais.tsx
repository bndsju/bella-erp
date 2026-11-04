import { useMemo } from "react";
import { GraficoBarras } from "@/components/dashboard/GraficoBarras";
import type { OpcaoSelecao } from "@/components/ui/selecao-pesquisavel";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { BotaoLimpar, CampoPeriodo, CampoPesquisa, CampoSelecao, CampoTamanho } from "@/components/relatorios/filtros";
import { TODOS, descrever, periodoObrigatorio, rotuloDe, sem, useFiltros, useLista, type Opcao } from "@/components/relatorios/filtrosBase";
import { Indicador, SemRegistros } from "@/components/relatorios/Indicador";
import { Paginacao } from "@/components/relatorios/Paginacao";
import { SecaoRelatorio } from "@/components/relatorios/SecaoRelatorio";
import { categoriaApi } from "@/lib/categoriaApi";
import { clienteApi } from "@/lib/clienteApi";
import { opcoesDeClientes } from "@/lib/clienteOpcoes";
import { formatarQuantidade } from "@/lib/estoqueFormatos";
import { FORMAS_PAGAMENTO, labelFormaPagamento } from "@/lib/formaPagamento";
import { formatarData, formatarMoeda } from "@/lib/formatos";
import { textoDoPeriodo, todasAsPaginas } from "@/lib/pdf";
import { produtoApi } from "@/lib/produtoApi";
import { opcoesDeProdutos } from "@/lib/produtoOpcoes";
import { relatorioApi } from "@/lib/relatorioApi";
import {
  ROTULO_STATUS_ORCAMENTO,
  ROTULO_STATUS_PEDIDO,
  formatarPercentual,
  rotuloDoPonto,
} from "@/lib/relatorioRotulos";

interface Props {
  inicio: string;
  fim: string;
}

interface Cadastros {
  clientes: OpcaoSelecao[];
  produtos: OpcaoSelecao[];
  categorias: Opcao[];
}

const LIMITES: Opcao[] = [
  { valor: "10", rotulo: "10 primeiros" },
  { valor: "20", rotulo: "20 primeiros" },
  { valor: "50", rotulo: "50 primeiros" },
];

function SecaoVendas({ inicio, fim, clientes, produtos }: Props & Pick<Cadastros, "clientes" | "produtos">) {
  const { filtros: f, alterado, mudar, irParaPagina, limpar } = useFiltros({
    inicio,
    fim,
    clienteId: TODOS,
    produtoId: TODOS,
    forma: TODOS,
    pagina: 0,
    tamanho: 10,
  });
  const params = {
    inicio: f.inicio,
    fim: f.fim,
    clienteId: sem(f.clienteId),
    produtoId: sem(f.produtoId),
    formaPagamento: sem(f.forma),
  };
  const formas: Opcao[] = FORMAS_PAGAMENTO.map((item) => ({ valor: item.value, rotulo: item.label }));

  return (
    <SecaoRelatorio
      titulo="Vendas"
      chave={JSON.stringify(f)}
      bloqueio={periodoObrigatorio(f.inicio, f.fim)}
      buscar={() => relatorioApi.vendas({ ...params, pagina: f.pagina, tamanho: f.tamanho })}
      exportar={async () => {
        const { primeira, itens } = await todasAsPaginas(
          (p, t) => relatorioApi.vendas({ ...params, pagina: p, tamanho: t }),
          (resposta) => resposta.vendas,
        );
        return {
          titulo: "Relatório de vendas",
          nomeArquivo: `vendas-${f.inicio}-a-${f.fim}`,
          periodo: textoDoPeriodo(f.inicio, f.fim),
          filtros: descrever([
            ["Cliente", rotuloDe(clientes, f.clienteId)],
            ["Produto", rotuloDe(produtos, f.produtoId)],
            ["Pagamento", rotuloDe(formas, f.forma)],
          ]),
          resumo: [
            ["Faturamento", formatarMoeda(primeira.resumo.faturamento)],
            ["Vendas", String(primeira.resumo.quantidade)],
            ["Ticket médio", formatarMoeda(primeira.resumo.ticketMedio)],
            ["Descontos", formatarMoeda(primeira.resumo.descontoTotal)],
            ["Frete", formatarMoeda(primeira.resumo.freteTotal)],
          ],
          tabelas: [
            {
              colunas: ["Data", "Cliente", "Pagamento", "Desconto", "Frete", "Total"],
              linhas: itens.map((venda) => [
                formatarData(venda.data),
                venda.clienteNome,
                labelFormaPagamento(venda.formaPagamento),
                formatarMoeda(venda.valorDesconto),
                formatarMoeda(venda.valorFrete),
                formatarMoeda(venda.valorTotal),
              ]),
              direita: [3, 4, 5],
            },
          ],
        };
      }}
      filtros={
        <>
          <CampoPeriodo prefixo="vendas" inicio={f.inicio} fim={f.fim} aoMudar={mudar} />
          <CampoPesquisa
            id="vendas-cliente"
            rotulo="Cliente"
            valor={f.clienteId}
            aoMudar={(clienteId) => mudar({ clienteId })}
            opcoes={clientes}
            rotuloTodos="Todos os clientes"
            placeholder="Buscar cliente"
          />
          <CampoPesquisa
            id="vendas-produto"
            rotulo="Produto vendido"
            valor={f.produtoId}
            aoMudar={(produtoId) => mudar({ produtoId })}
            opcoes={produtos}
            rotuloTodos="Todos os produtos"
            placeholder="Buscar por código ou nome"
          />
          <CampoSelecao
            id="vendas-forma"
            rotulo="Forma de pagamento"
            valor={f.forma}
            aoMudar={(forma) => mudar({ forma })}
            opcoes={formas}
            rotuloTodos="Todas"
          />
          <CampoTamanho id="vendas-tamanho" valor={f.tamanho} aoMudar={(tamanho) => mudar({ tamanho })} />
          <BotaoLimpar visivel={alterado} aoClicar={limpar} />
        </>
      }
    >
      {(dados) => (
        <>
          <div className="grade grade--4">
            <Indicador rotulo="Faturamento" valor={formatarMoeda(dados.resumo.faturamento)} />
            <Indicador rotulo="Vendas" valor={String(dados.resumo.quantidade)} />
            <Indicador rotulo="Ticket médio" valor={formatarMoeda(dados.resumo.ticketMedio)} />
            <Indicador
              rotulo="Descontos"
              valor={formatarMoeda(dados.resumo.descontoTotal)}
              nota={`Frete: ${formatarMoeda(dados.resumo.freteTotal)}`}
            />
          </div>
          <GraficoBarras
            series={[{ nome: "Vendas" }]}
            pontos={dados.serie.map((ponto) => ({
              rotulo: rotuloDoPonto(ponto.data, dados.granularidade),
              valores: [ponto.valor],
            }))}
            vazio={{ titulo: "Nenhuma venda no período", texto: "Ajuste o período ou os filtros." }}
          />
          {dados.vendas.itens.length === 0 ? (
            <SemRegistros texto="Nenhuma venda com esses filtros." />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Data</TableHead>
                  <TableHead>Cliente</TableHead>
                  <TableHead>Pagamento</TableHead>
                  <TableHead className="text-right">Desconto</TableHead>
                  <TableHead className="text-right">Frete</TableHead>
                  <TableHead className="text-right">Total</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.vendas.itens.map((venda) => (
                  <TableRow key={venda.id}>
                    <TableCell>{formatarData(venda.data)}</TableCell>
                    <TableCell className="tabela__forte">{venda.clienteNome}</TableCell>
                    <TableCell>{labelFormaPagamento(venda.formaPagamento)}</TableCell>
                    <TableCell className="text-right">{formatarMoeda(venda.valorDesconto)}</TableCell>
                    <TableCell className="text-right">{formatarMoeda(venda.valorFrete)}</TableCell>
                    <TableCell className="text-right">{formatarMoeda(venda.valorTotal)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <Paginacao pagina={dados.vendas} onMudar={irParaPagina} />
        </>
      )}
    </SecaoRelatorio>
  );
}

function SecaoPedidos({ inicio, fim, clientes }: Props & Pick<Cadastros, "clientes">) {
  const { filtros: f, alterado, mudar, limpar } = useFiltros({ inicio, fim, clienteId: TODOS, pagina: 0 });
  const params = { inicio: f.inicio, fim: f.fim, clienteId: sem(f.clienteId) };

  return (
    <SecaoRelatorio
      titulo="Pedidos por status"
      chave={JSON.stringify(f)}
      bloqueio={periodoObrigatorio(f.inicio, f.fim)}
      buscar={() => relatorioApi.pedidosPorStatus(params)}
      exportar={async () => {
        const dados = await relatorioApi.pedidosPorStatus(params);
        return {
          titulo: "Pedidos por status",
          nomeArquivo: `pedidos-por-status-${f.inicio}-a-${f.fim}`,
          periodo: textoDoPeriodo(f.inicio, f.fim),
          filtros: descrever([["Cliente", rotuloDe(clientes, f.clienteId)]]),
          resumo: [
            ["Pedidos (sem cancelados)", String(dados.quantidadeValidos)],
            ["Valor", formatarMoeda(dados.valorValidos)],
          ],
          tabelas: [
            {
              colunas: ["Status", "Pedidos", "Valor"],
              linhas: dados.porStatus.map((linha) => [
                ROTULO_STATUS_PEDIDO[linha.status],
                String(linha.quantidade),
                formatarMoeda(linha.valorTotal),
              ]),
              direita: [1, 2],
            },
          ],
        };
      }}
      filtros={
        <>
          <CampoPeriodo prefixo="pedidos" inicio={f.inicio} fim={f.fim} aoMudar={mudar} />
          <CampoPesquisa
            id="pedidos-cliente"
            rotulo="Cliente"
            valor={f.clienteId}
            aoMudar={(clienteId) => mudar({ clienteId })}
            opcoes={clientes}
            rotuloTodos="Todos os clientes"
            placeholder="Buscar cliente"
          />
          <BotaoLimpar visivel={alterado} aoClicar={limpar} />
        </>
      }
    >
      {(dados) =>
        dados.porStatus.length === 0 ? (
          <SemRegistros />
        ) : (
          <>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Pedidos</TableHead>
                  <TableHead className="text-right">Valor</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.porStatus.map((linha) => (
                  <TableRow key={linha.status}>
                    <TableCell className="tabela__forte">{ROTULO_STATUS_PEDIDO[linha.status]}</TableCell>
                    <TableCell className="text-right">{linha.quantidade}</TableCell>
                    <TableCell className="text-right">{formatarMoeda(linha.valorTotal)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <div className="linha">
              <strong>Total sem cancelados ({dados.quantidadeValidos})</strong>
              <strong>{formatarMoeda(dados.valorValidos)}</strong>
            </div>
          </>
        )
      }
    </SecaoRelatorio>
  );
}

function SecaoOrcamentos({ inicio, fim, clientes }: Props & Pick<Cadastros, "clientes">) {
  const { filtros: f, alterado, mudar, limpar } = useFiltros({ inicio, fim, clienteId: TODOS, pagina: 0 });
  const params = { inicio: f.inicio, fim: f.fim, clienteId: sem(f.clienteId) };

  return (
    <SecaoRelatorio
      titulo="Orçamentos"
      chave={JSON.stringify(f)}
      bloqueio={periodoObrigatorio(f.inicio, f.fim)}
      buscar={() => relatorioApi.orcamentos(params)}
      exportar={async () => {
        const dados = await relatorioApi.orcamentos(params);
        return {
          titulo: "Relatório de orçamentos",
          nomeArquivo: `orcamentos-${f.inicio}-a-${f.fim}`,
          periodo: textoDoPeriodo(f.inicio, f.fim),
          filtros: descrever([["Cliente", rotuloDe(clientes, f.clienteId)]]),
          resumo: [
            ["Emitidos", `${dados.emitidos} · ${formatarMoeda(dados.valorEmitidos)}`],
            ["Aprovados / recusados", `${dados.aprovados} / ${dados.recusados}`],
            ["Taxa de aprovação", formatarPercentual(dados.taxaAprovacao)],
            ["Viraram pedido", formatarPercentual(dados.taxaConversaoPedidos)],
            ["Viraram venda", formatarPercentual(dados.taxaConversaoVendas)],
          ],
          tabelas: [
            {
              colunas: ["Status", "Quantidade", "Valor"],
              linhas: dados.porStatus.map((linha) => [
                ROTULO_STATUS_ORCAMENTO[linha.status],
                String(linha.quantidade),
                formatarMoeda(linha.valorTotal),
              ]),
              direita: [1, 2],
            },
          ],
        };
      }}
      filtros={
        <>
          <CampoPeriodo prefixo="orcamentos" inicio={f.inicio} fim={f.fim} aoMudar={mudar} />
          <CampoPesquisa
            id="orcamentos-cliente"
            rotulo="Cliente"
            valor={f.clienteId}
            aoMudar={(clienteId) => mudar({ clienteId })}
            opcoes={clientes}
            rotuloTodos="Todos os clientes"
            placeholder="Buscar cliente"
          />
          <BotaoLimpar visivel={alterado} aoClicar={limpar} />
        </>
      }
    >
      {(dados) => (
        <>
          <div className="linha">
            <span>Emitidos</span>
            <span>
              {dados.emitidos} · {formatarMoeda(dados.valorEmitidos)}
            </span>
          </div>
          <div className="linha">
            <span>Aprovados / recusados</span>
            <span>
              {dados.aprovados} / {dados.recusados}
            </span>
          </div>
          <div className="linha">
            <span>Taxa de aprovação</span>
            <span>{formatarPercentual(dados.taxaAprovacao)}</span>
          </div>
          <div className="linha">
            <span>Viraram pedido</span>
            <span>{formatarPercentual(dados.taxaConversaoPedidos)}</span>
          </div>
          <div className="linha">
            <span>Viraram venda</span>
            <span>{formatarPercentual(dados.taxaConversaoVendas)}</span>
          </div>
          {dados.porStatus.length > 0 && (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Qtde</TableHead>
                  <TableHead className="text-right">Valor</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.porStatus.map((linha) => (
                  <TableRow key={linha.status}>
                    <TableCell className="tabela__forte">{ROTULO_STATUS_ORCAMENTO[linha.status]}</TableCell>
                    <TableCell className="text-right">{linha.quantidade}</TableCell>
                    <TableCell className="text-right">{formatarMoeda(linha.valorTotal)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </>
      )}
    </SecaoRelatorio>
  );
}

function SecaoProdutos({ inicio, fim, clientes, categorias }: Props & Pick<Cadastros, "clientes" | "categorias">) {
  const { filtros: f, alterado, mudar, limpar } = useFiltros({
    inicio,
    fim,
    clienteId: TODOS,
    categoriaId: TODOS,
    limite: "10",
    pagina: 0,
  });
  const params = {
    inicio: f.inicio,
    fim: f.fim,
    clienteId: sem(f.clienteId),
    categoriaId: sem(f.categoriaId),
    limite: Number(f.limite),
  };

  return (
    <SecaoRelatorio
      titulo="Produtos mais vendidos"
      chave={JSON.stringify(f)}
      bloqueio={periodoObrigatorio(f.inicio, f.fim)}
      buscar={() => relatorioApi.produtosMaisVendidos(params)}
      exportar={async () => {
        const dados = await relatorioApi.produtosMaisVendidos(params);
        return {
          titulo: "Produtos mais vendidos",
          nomeArquivo: `produtos-mais-vendidos-${f.inicio}-a-${f.fim}`,
          periodo: textoDoPeriodo(f.inicio, f.fim),
          filtros: descrever([
            ["Cliente", rotuloDe(clientes, f.clienteId)],
            ["Categoria", rotuloDe(categorias, f.categoriaId)],
            ["Exibindo", rotuloDe(LIMITES, f.limite)],
          ]),
          tabelas: [
            {
              colunas: ["Código", "Produto", "Categoria", "Quantidade", "Vendas", "Valor"],
              linhas: dados.produtos.map((produto) => [
                produto.codigoInterno,
                produto.nome,
                produto.categoriaNome ?? "—",
                formatarQuantidade(produto.quantidade),
                String(produto.quantidadeVendas),
                formatarMoeda(produto.valor),
              ]),
              direita: [3, 4, 5],
            },
          ],
        };
      }}
      filtros={
        <>
          <CampoPeriodo prefixo="produtos" inicio={f.inicio} fim={f.fim} aoMudar={mudar} />
          <CampoPesquisa
            id="produtos-cliente"
            rotulo="Cliente"
            valor={f.clienteId}
            aoMudar={(clienteId) => mudar({ clienteId })}
            opcoes={clientes}
            rotuloTodos="Todos os clientes"
            placeholder="Buscar cliente"
          />
          <CampoSelecao
            id="produtos-categoria"
            rotulo="Categoria"
            valor={f.categoriaId}
            aoMudar={(categoriaId) => mudar({ categoriaId })}
            opcoes={categorias}
            rotuloTodos="Todas"
          />
          <CampoSelecao
            id="produtos-limite"
            rotulo="Mostrar"
            valor={f.limite}
            aoMudar={(limite) => mudar({ limite })}
            opcoes={LIMITES}
          />
          <BotaoLimpar visivel={alterado} aoClicar={limpar} />
        </>
      }
    >
      {(dados) =>
        dados.produtos.length === 0 ? (
          <SemRegistros texto="Nenhum produto vendido com esses filtros." />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Produto</TableHead>
                <TableHead>Categoria</TableHead>
                <TableHead className="text-right">Qtde</TableHead>
                <TableHead className="text-right">Valor</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {dados.produtos.map((produto) => (
                <TableRow key={produto.produtoId}>
                  <TableCell className="tabela__forte">
                    {produto.codigoInterno} — {produto.nome}
                  </TableCell>
                  <TableCell>{produto.categoriaNome ?? "—"}</TableCell>
                  <TableCell className="text-right">{formatarQuantidade(produto.quantidade)}</TableCell>
                  <TableCell className="text-right">{formatarMoeda(produto.valor)}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )
      }
    </SecaoRelatorio>
  );
}

function SecaoClientes({ inicio, fim }: Props) {
  const { filtros: f, alterado, mudar, limpar } = useFiltros({ inicio, fim, limite: "10", pagina: 0 });
  const params = { inicio: f.inicio, fim: f.fim, limite: Number(f.limite) };

  return (
    <SecaoRelatorio
      titulo="Clientes de maior volume"
      chave={JSON.stringify(f)}
      bloqueio={periodoObrigatorio(f.inicio, f.fim)}
      buscar={() => relatorioApi.clientesMaiorVolume(params)}
      exportar={async () => {
        const dados = await relatorioApi.clientesMaiorVolume(params);
        return {
          titulo: "Clientes de maior volume",
          nomeArquivo: `clientes-maior-volume-${f.inicio}-a-${f.fim}`,
          periodo: textoDoPeriodo(f.inicio, f.fim),
          filtros: descrever([["Exibindo", rotuloDe(LIMITES, f.limite)]]),
          resumo: [["Faturamento total do período", formatarMoeda(dados.faturamentoTotal)]],
          tabelas: [
            {
              colunas: ["Cliente", "Vendas", "Valor", "Ticket médio", "Participação"],
              linhas: dados.clientes.map((cliente) => [
                cliente.nome,
                String(cliente.quantidadeVendas),
                formatarMoeda(cliente.valor),
                formatarMoeda(cliente.ticketMedio),
                formatarPercentual(cliente.participacaoPercentual),
              ]),
              direita: [1, 2, 3, 4],
            },
          ],
        };
      }}
      filtros={
        <>
          <CampoPeriodo prefixo="clientes" inicio={f.inicio} fim={f.fim} aoMudar={mudar} />
          <CampoSelecao
            id="clientes-limite"
            rotulo="Mostrar"
            valor={f.limite}
            aoMudar={(limite) => mudar({ limite })}
            opcoes={LIMITES}
          />
          <BotaoLimpar visivel={alterado} aoClicar={limpar} />
        </>
      }
    >
      {(dados) =>
        dados.clientes.length === 0 ? (
          <SemRegistros />
        ) : (
          <>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Cliente</TableHead>
                  <TableHead className="text-right">Vendas</TableHead>
                  <TableHead className="text-right">Valor</TableHead>
                  <TableHead className="text-right">Parte</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.clientes.map((cliente) => (
                  <TableRow key={cliente.clienteId}>
                    <TableCell className="tabela__forte">{cliente.nome}</TableCell>
                    <TableCell className="text-right">{cliente.quantidadeVendas}</TableCell>
                    <TableCell className="text-right">{formatarMoeda(cliente.valor)}</TableCell>
                    <TableCell className="text-right">{formatarPercentual(cliente.participacaoPercentual)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <div className="linha">
              <strong>Faturamento total do período</strong>
              <strong>{formatarMoeda(dados.faturamentoTotal)}</strong>
            </div>
          </>
        )
      }
    </SecaoRelatorio>
  );
}

export function RelatoriosComerciais({ inicio, fim }: Props) {
  const clientesBrutos = useLista(() => clienteApi.listar());
  const produtosBrutos = useLista(() => produtoApi.listar());
  const categoriasBrutas = useLista(() => categoriaApi.listar());
  const clientes = useMemo(() => opcoesDeClientes(clientesBrutos), [clientesBrutos]);
  const produtos = useMemo(() => opcoesDeProdutos(produtosBrutos), [produtosBrutos]);
  const categorias = useMemo<Opcao[]>(
    () => categoriasBrutas.map((categoria) => ({ valor: categoria.id, rotulo: categoria.nome })),
    [categoriasBrutas],
  );

  return (
    <>
      <SecaoVendas inicio={inicio} fim={fim} clientes={clientes} produtos={produtos} />
      <div className="grade grade--2">
        <SecaoPedidos inicio={inicio} fim={fim} clientes={clientes} />
        <SecaoOrcamentos inicio={inicio} fim={fim} clientes={clientes} />
      </div>
      <div className="grade grade--2">
        <SecaoProdutos inicio={inicio} fim={fim} clientes={clientes} categorias={categorias} />
        <SecaoClientes inicio={inicio} fim={fim} />
      </div>
    </>
  );
}
