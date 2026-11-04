import { useState } from "react";
import { GraficoBarras } from "@/components/dashboard/GraficoBarras";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Indicador, SemRegistros } from "@/components/relatorios/Indicador";
import { Paginacao } from "@/components/relatorios/Paginacao";
import { SecaoRelatorio } from "@/components/relatorios/SecaoRelatorio";
import { ROTULO_ORIGEM } from "@/lib/fluxoCaixaRotulos";
import { formatarData, formatarMoeda } from "@/lib/formatos";
import { relatorioApi } from "@/lib/relatorioApi";
import { ROTULO_STATUS_CONTA, rotuloDoPonto } from "@/lib/relatorioRotulos";

const TODOS = "TODOS";

interface Props {
  inicio: string;
  fim: string;
}

function SecaoContas({ tipo, inicio, fim }: Props & { tipo: "RECEBER" | "PAGAR" }) {
  const [pagina, setPagina] = useState(0);
  const receber = tipo === "RECEBER";
  return (
    <SecaoRelatorio
      titulo={receber ? "Contas a receber" : "Contas a pagar"}
      chave={`${tipo}|${inicio}|${fim}|${pagina}`}
      buscar={() =>
        (receber ? relatorioApi.contasReceber : relatorioApi.contasPagar)({ inicio, fim, pagina, tamanho: 10 })
      }
    >
      {(dados) => (
        <>
          <div className="grade grade--3">
            <Indicador
              rotulo={receber ? "A receber" : "A pagar"}
              valor={formatarMoeda(dados.totais.saldoPendente)}
              nota={`${dados.totais.quantidadeContas} conta(s) · total ${formatarMoeda(dados.totais.valorTotal)}`}
            />
            <Indicador rotulo="Vencido" valor={formatarMoeda(dados.totais.saldoVencido)} />
            <Indicador rotulo="A vencer" valor={formatarMoeda(dados.totais.saldoAVencer)} />
          </div>
          {dados.contas.itens.length === 0 ? (
            <SemRegistros texto="Nenhuma conta com vencimento neste período." />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Descrição</TableHead>
                  <TableHead>{receber ? "Cliente" : "Fornecedor"}</TableHead>
                  <TableHead>Vencimento</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Total</TableHead>
                  <TableHead className="text-right">Saldo</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.contas.itens.map((conta) => (
                  <TableRow key={conta.id}>
                    <TableCell className="tabela__forte">{conta.descricao}</TableCell>
                    <TableCell>{conta.parceiroNome || "—"}</TableCell>
                    <TableCell>{formatarData(conta.proximoVencimento)}</TableCell>
                    <TableCell>
                      {conta.vencida ? (
                        <Badge variant="destructive">Vencida</Badge>
                      ) : (
                        <Badge variant="outline">{ROTULO_STATUS_CONTA[conta.status] ?? conta.status}</Badge>
                      )}
                    </TableCell>
                    <TableCell className="text-right">{formatarMoeda(conta.valorTotal)}</TableCell>
                    <TableCell className="text-right">{formatarMoeda(conta.saldoPendente)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <Paginacao pagina={dados.contas} onMudar={setPagina} />
        </>
      )}
    </SecaoRelatorio>
  );
}

function SecaoVencimentos({ inicio, fim }: Props) {
  const [tipo, setTipo] = useState(TODOS);
  const [apenasVencidos, setApenasVencidos] = useState(false);
  const [pagina, setPagina] = useState(0);
  return (
    <SecaoRelatorio
      titulo="Vencimentos"
      chave={`${inicio}|${fim}|${tipo}|${apenasVencidos}|${pagina}`}
      buscar={() =>
        relatorioApi.vencimentos({
          inicio,
          fim,
          tipo: tipo === TODOS ? undefined : tipo,
          apenasVencidos,
          pagina,
          tamanho: 10,
        })
      }
      filtros={
        <>
          <div className="campo">
            <label htmlFor="rel-venc-tipo">Tipo</label>
            <Select
              value={tipo}
              onValueChange={(valor) => {
                setTipo(valor);
                setPagina(0);
              }}
            >
              <SelectTrigger id="rel-venc-tipo" className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={TODOS}>Receber e pagar</SelectItem>
                <SelectItem value="RECEBER">A receber</SelectItem>
                <SelectItem value="PAGAR">A pagar</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <label className="flex min-h-11 items-center gap-2 text-sm font-semibold">
            <input
              type="checkbox"
              checked={apenasVencidos}
              onChange={(e) => {
                setApenasVencidos(e.target.checked);
                setPagina(0);
              }}
            />
            Só vencidos
          </label>
        </>
      }
    >
      {(dados) => (
        <>
          <div className="grade grade--4">
            <Indicador rotulo="Receber vencido" valor={formatarMoeda(dados.totais.vencidoReceber)} />
            <Indicador rotulo="Receber a vencer" valor={formatarMoeda(dados.totais.aVencerReceber)} />
            <Indicador rotulo="Pagar vencido" valor={formatarMoeda(dados.totais.vencidoPagar)} />
            <Indicador rotulo="Pagar a vencer" valor={formatarMoeda(dados.totais.aVencerPagar)} />
          </div>
          {dados.faixas.length > 0 && (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Faixa</TableHead>
                  <TableHead className="text-right">A receber</TableHead>
                  <TableHead className="text-right">A pagar</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.faixas.map((faixa) => (
                  <TableRow key={faixa.faixa}>
                    <TableCell className="tabela__forte">{faixa.faixa}</TableCell>
                    <TableCell className="text-right">{formatarMoeda(faixa.receber)}</TableCell>
                    <TableCell className="text-right">{formatarMoeda(faixa.pagar)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          {dados.titulos.itens.length === 0 ? (
            <SemRegistros texto="Nenhum título com saldo neste período." />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Descrição</TableHead>
                  <TableHead>Parte</TableHead>
                  <TableHead>Parcela</TableHead>
                  <TableHead>Vencimento</TableHead>
                  <TableHead className="text-right">Atraso</TableHead>
                  <TableHead className="text-right">Saldo</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.titulos.itens.map((titulo) => (
                  <TableRow key={`${titulo.contaId}-${titulo.parcela}`}>
                    <TableCell>{titulo.tipo === "RECEBER" ? "Receber" : "Pagar"}</TableCell>
                    <TableCell className="tabela__forte">{titulo.descricao}</TableCell>
                    <TableCell>{titulo.parceiroNome || "—"}</TableCell>
                    <TableCell>{titulo.parcela}</TableCell>
                    <TableCell>{formatarData(titulo.vencimento)}</TableCell>
                    <TableCell className="text-right">
                      {titulo.diasEmAtraso > 0 ? `${titulo.diasEmAtraso} dia(s)` : "—"}
                    </TableCell>
                    <TableCell className="text-right">{formatarMoeda(titulo.saldoPendente)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <Paginacao pagina={dados.titulos} onMudar={setPagina} />
        </>
      )}
    </SecaoRelatorio>
  );
}

function SecaoFluxo({ inicio, fim }: Props) {
  const [situacao, setSituacao] = useState(TODOS);
  const [pagina, setPagina] = useState(0);
  return (
    <SecaoRelatorio
      titulo="Fluxo de caixa"
      chave={`${inicio}|${fim}|${situacao}|${pagina}`}
      buscar={() =>
        relatorioApi.fluxoCaixa({
          inicio,
          fim,
          situacao: situacao === TODOS ? undefined : situacao,
          pagina,
          tamanho: 10,
        })
      }
      filtros={
        <div className="campo">
          <label htmlFor="rel-fluxo-situacao">Situação (movimentações)</label>
          <Select
            value={situacao}
            onValueChange={(valor) => {
              setSituacao(valor);
              setPagina(0);
            }}
          >
            <SelectTrigger id="rel-fluxo-situacao" className="w-full">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={TODOS}>Todas</SelectItem>
              <SelectItem value="REALIZADO">Realizado</SelectItem>
              <SelectItem value="PREVISTO">Previsto</SelectItem>
            </SelectContent>
          </Select>
        </div>
      }
    >
      {(dados) => (
        <>
          <div className="grade grade--4">
            <Indicador
              rotulo="Entradas realizadas"
              valor={formatarMoeda(dados.resumo.entradasRealizadas)}
              nota={`Previstas: ${formatarMoeda(dados.resumo.entradasPrevistas)}`}
            />
            <Indicador
              rotulo="Saídas realizadas"
              valor={formatarMoeda(dados.resumo.saidasRealizadas)}
              nota={`Previstas: ${formatarMoeda(dados.resumo.saidasPrevistas)}`}
            />
            <Indicador
              rotulo="Saldo realizado"
              valor={formatarMoeda(dados.resumo.saldoRealizado)}
              nota={`Projetado: ${formatarMoeda(dados.resumo.saldoProjetado)}`}
            />
            <Indicador rotulo="Saldo acumulado" valor={formatarMoeda(dados.saldoAcumuladoAteFim)} nota="Realizado até o fim do período" />
          </div>
          <GraficoBarras
            series={[
              { nome: "Entradas", cor: "var(--grafico-2)" },
              { nome: "Saídas", cor: "var(--grafico-4)" },
            ]}
            pontos={dados.serie.map((ponto) => ({
              rotulo: rotuloDoPonto(ponto.data, dados.granularidade),
              valores: [ponto.entradas, ponto.saidas],
            }))}
            vazio={{ titulo: "Nenhuma movimentação no período", texto: "Entradas e saídas realizadas aparecem aqui." }}
          />
          {dados.porCategoria.length > 0 && (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Categoria</TableHead>
                  <TableHead className="text-right">Entradas</TableHead>
                  <TableHead className="text-right">Saídas</TableHead>
                  <TableHead className="text-right">Saldo realizado</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.porCategoria.map((linha) => (
                  <TableRow key={linha.categoriaId ?? "sem-categoria"}>
                    <TableCell className="tabela__forte">{linha.categoriaNome ?? "Sem categoria"}</TableCell>
                    <TableCell className="text-right">{formatarMoeda(linha.resumo.entradasRealizadas)}</TableCell>
                    <TableCell className="text-right">{formatarMoeda(linha.resumo.saidasRealizadas)}</TableCell>
                    <TableCell className="text-right">{formatarMoeda(linha.resumo.saldoRealizado)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          {dados.movimentacoes.itens.length === 0 ? (
            <SemRegistros texto="Nenhuma movimentação neste período." />
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Data</TableHead>
                  <TableHead>Descrição</TableHead>
                  <TableHead>Categoria</TableHead>
                  <TableHead>Origem</TableHead>
                  <TableHead>Situação</TableHead>
                  <TableHead className="text-right">Valor</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {dados.movimentacoes.itens.map((mov) => (
                  <TableRow key={mov.id}>
                    <TableCell>{formatarData(mov.data)}</TableCell>
                    <TableCell className="tabela__forte">{mov.descricao}</TableCell>
                    <TableCell>{mov.categoriaNome ?? "—"}</TableCell>
                    <TableCell>{ROTULO_ORIGEM[mov.origem]}</TableCell>
                    <TableCell>
                      <Badge variant={mov.situacao === "REALIZADO" ? "default" : "outline"}>
                        {mov.situacao === "REALIZADO" ? "Realizado" : "Previsto"}
                      </Badge>
                    </TableCell>
                    <TableCell className={mov.tipo === "SAIDA" ? "text-right text-erro" : "text-right"}>
                      {mov.tipo === "SAIDA" ? "- " : ""}
                      {formatarMoeda(mov.valor)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
          <Paginacao pagina={dados.movimentacoes} onMudar={setPagina} />
        </>
      )}
    </SecaoRelatorio>
  );
}

export function RelatoriosFinanceiros({ inicio, fim }: Props) {
  return (
    <>
      <SecaoFluxo inicio={inicio} fim={fim} />
      <SecaoContas tipo="RECEBER" inicio={inicio} fim={fim} />
      <SecaoContas tipo="PAGAR" inicio={inicio} fim={fim} />
      <SecaoVencimentos inicio={inicio} fim={fim} />
    </>
  );
}
