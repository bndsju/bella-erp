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
import { opcoesDeClientes } from "@/lib/clienteOpcoes";
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
