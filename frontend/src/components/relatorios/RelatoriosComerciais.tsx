import { useEffect, useMemo, useState } from "react";
import { GraficoBarras } from "@/components/dashboard/GraficoBarras";
import { Card } from "@/components/ui/card";
import { SelecaoPesquisavel } from "@/components/ui/selecao-pesquisavel";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Indicador, SemRegistros } from "@/components/relatorios/Indicador";
import { Paginacao } from "@/components/relatorios/Paginacao";
import { SecaoRelatorio } from "@/components/relatorios/SecaoRelatorio";
import { clienteApi } from "@/lib/clienteApi";
import { nomeDoCliente, opcoesDeClientes } from "@/lib/clienteOpcoes";
import { todasAsPaginas, textoDoPeriodo } from "@/lib/pdf";
import { formatarData, formatarMoeda } from "@/lib/formatos";
import { FORMAS_PAGAMENTO, labelFormaPagamento } from "@/lib/formaPagamento";
import { formatarQuantidade } from "@/lib/estoqueFormatos";
import { relatorioApi } from "@/lib/relatorioApi";
import {
  ROTULO_STATUS_ORCAMENTO,
  ROTULO_STATUS_PEDIDO,
  formatarPercentual,
  rotuloDoPonto,
} from "@/lib/relatorioRotulos";
import type { ClienteResponse } from "@/types/cliente";
import type { FormaPagamento } from "@/types/venda";

const TODOS = "TODOS";

interface Props {
  inicio: string;
  fim: string;
}

export function RelatoriosComerciais({ inicio, fim }: Props) {
  const [clientes, setClientes] = useState<ClienteResponse[]>([]);
  const [clienteId, setClienteId] = useState(TODOS);
  const [forma, setForma] = useState(TODOS);
  const [pagina, setPagina] = useState(0);
  const opcoes = useMemo(() => opcoesDeClientes(clientes), [clientes]);

  useEffect(() => {
    clienteApi.listar().then(setClientes).catch(() => setClientes([]));
  }, []);

  const filtroCliente = clienteId === TODOS ? undefined : clienteId;
  const base = { inicio, fim, clienteId: filtroCliente };
  const periodo = textoDoPeriodo(inicio, fim);
  const clienteEscolhido = clientes.find((cliente) => cliente.id === clienteId);
  const filtrosCliente = clienteEscolhido ? [`Cliente: ${nomeDoCliente(clienteEscolhido)}`] : [];
  const arquivo = (nome: string) => `${nome}-${inicio}-a-${fim}`;

  return (
    <>
      <Card>
        <div className="barra-filtros">
          <div className="campo">
            <label htmlFor="rel-cliente">Cliente</label>
            <SelecaoPesquisavel
              id="rel-cliente"
              value={clienteId}
              onChange={(valor) => {
                setClienteId(valor);
                setPagina(0);
              }}
              opcoes={opcoes}
              valorTodos={TODOS}
              rotuloTodos="Todos os clientes"
              placeholderBusca="Buscar cliente"
            />
          </div>
          <p className="self-end pb-3 text-[13px] text-muted-foreground">
            O cliente vale para vendas, pedidos, orçamentos e produtos. O ranking de clientes é sempre geral.
          </p>
        </div>
      </Card>

      <SecaoRelatorio
        titulo="Vendas"
        chave={`${inicio}|${fim}|${clienteId}|${forma}|${pagina}`}
        exportar={async () => {
          const { primeira, itens } = await todasAsPaginas(
            (p, t) =>
              relatorioApi.vendas({
                ...base,
                formaPagamento: forma === TODOS ? undefined : forma,
                pagina: p,
                tamanho: t,
              }),
            (resposta) => resposta.vendas,
          );
          return {
            titulo: "Relatório de vendas",
            nomeArquivo: arquivo("vendas"),
            periodo,
            filtros: [
              ...filtrosCliente,
              ...(forma === TODOS ? [] : [`Pagamento: ${labelFormaPagamento(forma as FormaPagamento)}`]),
            ],
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
        buscar={() =>
          relatorioApi.vendas({
            ...base,
            formaPagamento: forma === TODOS ? undefined : forma,
            pagina,
            tamanho: 10,
          })
        }
        filtros={
          <div className="campo">
            <label htmlFor="rel-forma">Forma de pagamento</label>
            <Select
              value={forma}
              onValueChange={(valor) => {
                setForma(valor);
                setPagina(0);
              }}
            >
              <SelectTrigger id="rel-forma" className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={TODOS}>Todas</SelectItem>
                {FORMAS_PAGAMENTO.map((item) => (
                  <SelectItem key={item.value} value={item.value}>
                    {item.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
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
              <SemRegistros />
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
            <Paginacao pagina={dados.vendas} onMudar={setPagina} />
          </>
        )}
      </SecaoRelatorio>

      <div className="grade grade--2">
        <SecaoRelatorio
          titulo="Pedidos por status"
          chave={`${inicio}|${fim}|${clienteId}`}
          buscar={() => relatorioApi.pedidosPorStatus(base)}
          exportar={async () => {
            const dados = await relatorioApi.pedidosPorStatus(base);
            return {
              titulo: "Pedidos por status",
              nomeArquivo: arquivo("pedidos-por-status"),
              periodo,
              filtros: filtrosCliente,
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

        <SecaoRelatorio
          titulo="Orçamentos"
          chave={`${inicio}|${fim}|${clienteId}`}
          buscar={() => relatorioApi.orcamentos(base)}
          exportar={async () => {
            const dados = await relatorioApi.orcamentos(base);
            return {
              titulo: "Relatório de orçamentos",
              nomeArquivo: arquivo("orcamentos"),
              periodo,
              filtros: filtrosCliente,
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
      </div>

      <div className="grade grade--2">
        <SecaoRelatorio
          titulo="Produtos mais vendidos"
          chave={`${inicio}|${fim}|${clienteId}`}
          buscar={() => relatorioApi.produtosMaisVendidos({ ...base, limite: 10 })}
          exportar={async () => {
            const dados = await relatorioApi.produtosMaisVendidos({ ...base, limite: 50 });
            return {
              titulo: "Produtos mais vendidos",
              nomeArquivo: arquivo("produtos-mais-vendidos"),
              periodo,
              filtros: filtrosCliente,
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
        >
          {(dados) =>
            dados.produtos.length === 0 ? (
              <SemRegistros />
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

        <SecaoRelatorio
          titulo="Clientes de maior volume"
          chave={`${inicio}|${fim}`}
          buscar={() => relatorioApi.clientesMaiorVolume({ inicio, fim, limite: 10 })}
          exportar={async () => {
            const dados = await relatorioApi.clientesMaiorVolume({ inicio, fim, limite: 50 });
            return {
              titulo: "Clientes de maior volume",
              nomeArquivo: arquivo("clientes-maior-volume"),
              periodo,
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
      </div>
    </>
  );
}
