import { useMemo } from "react";
import { GraficoBarras } from "@/components/dashboard/GraficoBarras";
import { Badge } from "@/components/ui/badge";
import type { OpcaoSelecao } from "@/components/ui/selecao-pesquisavel";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { BotaoLimpar, CampoMarcar, CampoPeriodo, CampoPesquisa, CampoSelecao, CampoTamanho } from "@/components/relatorios/filtros";
import { TODOS, descrever, periodoInvalido, periodoObrigatorio, rotuloDe, sem, useFiltros, useLista, type Opcao } from "@/components/relatorios/filtrosBase";
import { Indicador, SemRegistros } from "@/components/relatorios/Indicador";
import { Paginacao } from "@/components/relatorios/Paginacao";
import { SecaoRelatorio } from "@/components/relatorios/SecaoRelatorio";
import { categoriaDespesaApi } from "@/lib/categoriaDespesaApi";
import { categoriaFluxoCaixaApi } from "@/lib/categoriaFluxoCaixaApi";
import { clienteApi } from "@/lib/clienteApi";
import { opcoesDeClientes } from "@/lib/clienteOpcoes";
import { fornecedorApi } from "@/lib/fornecedorApi";
import { ROTULO_ORIGEM } from "@/lib/fluxoCaixaRotulos";
import { formatarData, formatarMoeda } from "@/lib/formatos";
import { textoDoPeriodo, textoDoPeriodoAberto, todasAsPaginas } from "@/lib/pdf";
import { relatorioApi } from "@/lib/relatorioApi";
import { ROTULO_STATUS_CONTA, rotuloDoPonto } from "@/lib/relatorioRotulos";

interface Props {
  inicio: string;
  fim: string;
}

const STATUS_RECEBER: Opcao[] = ["PENDENTE", "PARCIALMENTE_RECEBIDA", "RECEBIDA", "CANCELADA"].map((valor) => ({
  valor,
  rotulo: ROTULO_STATUS_CONTA[valor],
}));
const STATUS_PAGAR: Opcao[] = ["PENDENTE", "PARCIALMENTE_PAGA", "PAGA", "CANCELADA"].map((valor) => ({
  valor,
  rotulo: ROTULO_STATUS_CONTA[valor],
}));
const SITUACAO_RECEBER: Opcao[] = [
  { valor: "EM_ABERTO", rotulo: "Em aberto" },
  { valor: "VENCIDAS", rotulo: "Vencidas" },
  { valor: "RECEBIDAS", rotulo: "Recebidas" },
];
const SITUACAO_PAGAR: Opcao[] = [
  { valor: "EM_ABERTO", rotulo: "Em aberto" },
  { valor: "VENCIDAS", rotulo: "Vencidas" },
  { valor: "PAGAS", rotulo: "Pagas" },
];
const TIPO_CAIXA: Opcao[] = [
  { valor: "ENTRADA", rotulo: "Entradas" },
  { valor: "SAIDA", rotulo: "Saídas" },
];
const ORIGEM_CAIXA: Opcao[] = [
  { valor: "CONTA_RECEBER", rotulo: ROTULO_ORIGEM.CONTA_RECEBER },
  { valor: "CONTA_PAGAR", rotulo: ROTULO_ORIGEM.CONTA_PAGAR },
  { valor: "LANCAMENTO_MANUAL", rotulo: ROTULO_ORIGEM.LANCAMENTO_MANUAL },
];
const SITUACAO_CAIXA: Opcao[] = [
  { valor: "REALIZADO", rotulo: "Realizado" },
  { valor: "PREVISTO", rotulo: "Previsto" },
];
const TIPO_TITULO: Opcao[] = [
  { valor: "RECEBER", rotulo: "A receber" },
  { valor: "PAGAR", rotulo: "A pagar" },
];

function SecaoContas({
  tipo,
  inicio,
  fim,
  parceiros,
  categorias,
}: Props & { tipo: "RECEBER" | "PAGAR"; parceiros: OpcaoSelecao[]; categorias: Opcao[] }) {
  const receber = tipo === "RECEBER";
  const { filtros: f, alterado, mudar, irParaPagina, limpar } = useFiltros({
    inicio,
    fim,
    parceiroId: TODOS,
    categoriaId: TODOS,
    status: TODOS,
    situacao: TODOS,
    pagina: 0,
    tamanho: 10,
  });
  const params = receber
    ? { inicio: f.inicio, fim: f.fim, clienteId: sem(f.parceiroId), status: sem(f.status), situacao: sem(f.situacao) }
    : {
        inicio: f.inicio,
        fim: f.fim,
        fornecedorId: sem(f.parceiroId),
        categoriaDespesaId: sem(f.categoriaId),
        status: sem(f.status),
        situacao: sem(f.situacao),
      };
  const consultar = receber ? relatorioApi.contasReceber : relatorioApi.contasPagar;
  const statusOpcoes = receber ? STATUS_RECEBER : STATUS_PAGAR;
  const situacaoOpcoes = receber ? SITUACAO_RECEBER : SITUACAO_PAGAR;
  const prefixo = receber ? "receber" : "pagar";

  return (
    <SecaoRelatorio
      titulo={receber ? "Contas a receber" : "Contas a pagar"}
      chave={JSON.stringify(f)}
      bloqueio={periodoInvalido(f.inicio, f.fim)}
      buscar={() => consultar({ ...params, pagina: f.pagina, tamanho: f.tamanho })}
      exportar={async () => {
        const { primeira, itens } = await todasAsPaginas(
          (p, t) => consultar({ ...params, pagina: p, tamanho: t }),
          (resposta) => resposta.contas,
        );
        return {
          titulo: receber ? "Contas a receber" : "Contas a pagar",
          nomeArquivo: `contas-${prefixo}${f.inicio ? `-${f.inicio}` : ""}${f.fim ? `-a-${f.fim}` : ""}`,
          periodo: textoDoPeriodoAberto(f.inicio, f.fim),
          filtros: descrever([
            [receber ? "Cliente" : "Fornecedor", rotuloDe(parceiros, f.parceiroId)],
            ["Categoria", receber ? undefined : rotuloDe(categorias, f.categoriaId)],
            ["Status", rotuloDe(statusOpcoes, f.status)],
            ["Situação", rotuloDe(situacaoOpcoes, f.situacao)],
          ]),
          resumo: [
            [receber ? "A receber" : "A pagar", formatarMoeda(primeira.totais.saldoPendente)],
            ["Vencido", formatarMoeda(primeira.totais.saldoVencido)],
            ["A vencer", formatarMoeda(primeira.totais.saldoAVencer)],
            ["Contas", String(primeira.totais.quantidadeContas)],
            ["Valor total", formatarMoeda(primeira.totais.valorTotal)],
          ],
          tabelas: [
            {
              colunas: ["Descrição", receber ? "Cliente" : "Fornecedor", "Vencimento", "Status", "Total", "Saldo"],
              linhas: itens.map((conta) => [
                conta.descricao,
                conta.parceiroNome || "—",
                formatarData(conta.proximoVencimento),
                conta.vencida ? "Vencida" : (ROTULO_STATUS_CONTA[conta.status] ?? conta.status),
                formatarMoeda(conta.valorTotal),
                formatarMoeda(conta.saldoPendente),
              ]),
              direita: [4, 5],
            },
          ],
        };
      }}
      filtros={
        <>
          <CampoPeriodo
            prefixo={`contas-${prefixo}`}
            rotuloDe="Vencimento de"
            inicio={f.inicio}
            fim={f.fim}
            aoMudar={mudar}
          />
          <CampoPesquisa
            id={`contas-${prefixo}-parceiro`}
            rotulo={receber ? "Cliente" : "Fornecedor"}
            valor={f.parceiroId}
            aoMudar={(parceiroId) => mudar({ parceiroId })}
            opcoes={parceiros}
            rotuloTodos={receber ? "Todos os clientes" : "Todos os fornecedores"}
            placeholder={receber ? "Buscar cliente" : "Buscar fornecedor"}
          />
          {!receber && (
            <CampoSelecao
              id="contas-pagar-categoria"
              rotulo="Categoria de despesa"
              valor={f.categoriaId}
              aoMudar={(categoriaId) => mudar({ categoriaId })}
              opcoes={categorias}
              rotuloTodos="Todas"
            />
          )}
          <CampoSelecao
            id={`contas-${prefixo}-status`}
            rotulo="Status"
            valor={f.status}
            aoMudar={(status) => mudar({ status })}
            opcoes={statusOpcoes}
            rotuloTodos="Todos"
          />
          <CampoSelecao
            id={`contas-${prefixo}-situacao`}
            rotulo="Situação"
            valor={f.situacao}
            aoMudar={(situacao) => mudar({ situacao })}
            opcoes={situacaoOpcoes}
            rotuloTodos="Todas"
          />
          <CampoTamanho id={`contas-${prefixo}-tamanho`} valor={f.tamanho} aoMudar={(tamanho) => mudar({ tamanho })} />
          <BotaoLimpar visivel={alterado} aoClicar={limpar} />
        </>
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
            <SemRegistros texto="Nenhuma conta com esses filtros." />
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
          <Paginacao pagina={dados.contas} onMudar={irParaPagina} />
        </>
      )}
    </SecaoRelatorio>
  );
}

function SecaoVencimentos({ inicio, fim }: Props) {
  const { filtros: f, alterado, mudar, irParaPagina, limpar } = useFiltros({
    inicio,
    fim,
    tipo: TODOS,
    apenasVencidos: false,
    pagina: 0,
    tamanho: 10,
  });
  const params = { inicio: f.inicio, fim: f.fim, tipo: sem(f.tipo), apenasVencidos: f.apenasVencidos };

  return (
    <SecaoRelatorio
      titulo="Vencimentos"
      chave={JSON.stringify(f)}
      bloqueio={periodoInvalido(f.inicio, f.fim)}
      buscar={() => relatorioApi.vencimentos({ ...params, pagina: f.pagina, tamanho: f.tamanho })}
      exportar={async () => {
        const { primeira, itens } = await todasAsPaginas(
          (p, t) => relatorioApi.vencimentos({ ...params, pagina: p, tamanho: t }),
          (resposta) => resposta.titulos,
        );
        return {
          titulo: "Vencimentos",
          nomeArquivo: `vencimentos${f.inicio ? `-${f.inicio}` : ""}${f.fim ? `-a-${f.fim}` : ""}`,
          periodo: textoDoPeriodoAberto(f.inicio, f.fim),
          filtros: descrever([
            ["Tipo", rotuloDe(TIPO_TITULO, f.tipo)],
            ["Somente", f.apenasVencidos ? "vencidos" : undefined],
          ]),
          resumo: [
            ["Receber vencido", formatarMoeda(primeira.totais.vencidoReceber)],
            ["Receber a vencer", formatarMoeda(primeira.totais.aVencerReceber)],
            ["Pagar vencido", formatarMoeda(primeira.totais.vencidoPagar)],
            ["Pagar a vencer", formatarMoeda(primeira.totais.aVencerPagar)],
          ],
          tabelas: [
            {
              titulo: "Por faixa de vencimento",
              colunas: ["Faixa", "A receber", "A pagar"],
              linhas: primeira.faixas.map((faixa) => [
                faixa.faixa,
                formatarMoeda(faixa.receber),
                formatarMoeda(faixa.pagar),
              ]),
              direita: [1, 2],
            },
            {
              titulo: "Títulos",
              colunas: ["Tipo", "Descrição", "Parte", "Parcela", "Vencimento", "Atraso", "Saldo"],
              linhas: itens.map((titulo) => [
                titulo.tipo === "RECEBER" ? "Receber" : "Pagar",
                titulo.descricao,
                titulo.parceiroNome || "—",
                String(titulo.parcela),
                formatarData(titulo.vencimento),
                titulo.diasEmAtraso > 0 ? `${titulo.diasEmAtraso} dia(s)` : "—",
                formatarMoeda(titulo.saldoPendente),
              ]),
              direita: [5, 6],
            },
          ],
        };
      }}
      filtros={
        <>
          <CampoPeriodo prefixo="venc" rotuloDe="Vencimento de" inicio={f.inicio} fim={f.fim} aoMudar={mudar} />
          <CampoSelecao
            id="venc-tipo"
            rotulo="Tipo"
            valor={f.tipo}
            aoMudar={(tipo) => mudar({ tipo })}
            opcoes={TIPO_TITULO}
            rotuloTodos="Receber e pagar"
          />
          <CampoMarcar
            rotulo="Só vencidos"
            marcado={f.apenasVencidos}
            aoMudar={(apenasVencidos) => mudar({ apenasVencidos })}
          />
          <CampoTamanho id="venc-tamanho" valor={f.tamanho} aoMudar={(tamanho) => mudar({ tamanho })} />
          <BotaoLimpar visivel={alterado} aoClicar={limpar} />
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
            <SemRegistros texto="Nenhum título com saldo com esses filtros." />
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
          <Paginacao pagina={dados.titulos} onMudar={irParaPagina} />
        </>
      )}
    </SecaoRelatorio>
  );
}

function SecaoFluxo({ inicio, fim, categorias }: Props & { categorias: Opcao[] }) {
  const { filtros: f, alterado, mudar, irParaPagina, limpar } = useFiltros({
    inicio,
    fim,
    tipo: TODOS,
    categoriaId: TODOS,
    origem: TODOS,
    situacao: TODOS,
    pagina: 0,
    tamanho: 10,
  });
  const params = {
    inicio: f.inicio,
    fim: f.fim,
    tipo: sem(f.tipo),
    categoriaId: sem(f.categoriaId),
    origem: sem(f.origem),
    situacao: sem(f.situacao),
  };

  return (
    <SecaoRelatorio
      titulo="Fluxo de caixa"
      chave={JSON.stringify(f)}
      bloqueio={periodoObrigatorio(f.inicio, f.fim)}
      buscar={() => relatorioApi.fluxoCaixa({ ...params, pagina: f.pagina, tamanho: f.tamanho })}
      exportar={async () => {
        const { primeira, itens } = await todasAsPaginas(
          (p, t) => relatorioApi.fluxoCaixa({ ...params, pagina: p, tamanho: t }),
          (resposta) => resposta.movimentacoes,
        );
        return {
          titulo: "Fluxo de caixa",
          nomeArquivo: `fluxo-de-caixa-${f.inicio}-a-${f.fim}`,
          periodo: textoDoPeriodo(f.inicio, f.fim),
          filtros: descrever([
            ["Tipo", rotuloDe(TIPO_CAIXA, f.tipo)],
            ["Categoria", rotuloDe(categorias, f.categoriaId)],
            ["Origem", rotuloDe(ORIGEM_CAIXA, f.origem)],
            ["Situação", rotuloDe(SITUACAO_CAIXA, f.situacao)],
          ]),
          resumo: [
            ["Entradas realizadas", formatarMoeda(primeira.resumo.entradasRealizadas)],
            ["Saídas realizadas", formatarMoeda(primeira.resumo.saidasRealizadas)],
            ["Saldo realizado", formatarMoeda(primeira.resumo.saldoRealizado)],
            ["Saldo projetado", formatarMoeda(primeira.resumo.saldoProjetado)],
            ["Saldo acumulado", formatarMoeda(primeira.saldoAcumuladoAteFim)],
          ],
          tabelas: [
            {
              titulo: "Por categoria",
              colunas: ["Categoria", "Entradas", "Saídas", "Saldo realizado"],
              linhas: primeira.porCategoria.map((linha) => [
                linha.categoriaNome ?? "Sem categoria",
                formatarMoeda(linha.resumo.entradasRealizadas),
                formatarMoeda(linha.resumo.saidasRealizadas),
                formatarMoeda(linha.resumo.saldoRealizado),
              ]),
              direita: [1, 2, 3],
            },
            {
              titulo: "Movimentações",
              colunas: ["Data", "Descrição", "Categoria", "Origem", "Situação", "Valor"],
              linhas: itens.map((mov) => [
                formatarData(mov.data),
                mov.descricao,
                mov.categoriaNome ?? "—",
                ROTULO_ORIGEM[mov.origem],
                mov.situacao === "REALIZADO" ? "Realizado" : "Previsto",
                `${mov.tipo === "SAIDA" ? "- " : ""}${formatarMoeda(mov.valor)}`,
              ]),
              direita: [5],
            },
          ],
        };
      }}
      filtros={
        <>
          <CampoPeriodo prefixo="fluxo" inicio={f.inicio} fim={f.fim} aoMudar={mudar} />
          <CampoSelecao
            id="fluxo-tipo"
            rotulo="Tipo"
            valor={f.tipo}
            aoMudar={(tipo) => mudar({ tipo })}
            opcoes={TIPO_CAIXA}
            rotuloTodos="Entradas e saídas"
          />
          <CampoSelecao
            id="fluxo-categoria"
            rotulo="Categoria"
            valor={f.categoriaId}
            aoMudar={(categoriaId) => mudar({ categoriaId })}
            opcoes={categorias}
            rotuloTodos="Todas"
          />
          <CampoSelecao
            id="fluxo-origem"
            rotulo="Origem"
            valor={f.origem}
            aoMudar={(origem) => mudar({ origem })}
            opcoes={ORIGEM_CAIXA}
            rotuloTodos="Todas"
          />
          <CampoSelecao
            id="fluxo-situacao"
            rotulo="Situação"
            valor={f.situacao}
            aoMudar={(situacao) => mudar({ situacao })}
            opcoes={SITUACAO_CAIXA}
            rotuloTodos="Todas"
          />
          <CampoTamanho id="fluxo-tamanho" valor={f.tamanho} aoMudar={(tamanho) => mudar({ tamanho })} />
          <BotaoLimpar visivel={alterado} aoClicar={limpar} />
        </>
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
            <Indicador
              rotulo="Saldo acumulado"
              valor={formatarMoeda(dados.saldoAcumuladoAteFim)}
              nota="Realizado até o fim do período"
            />
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
            <SemRegistros texto="Nenhuma movimentação com esses filtros." />
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
          <Paginacao pagina={dados.movimentacoes} onMudar={irParaPagina} />
        </>
      )}
    </SecaoRelatorio>
  );
}

export function RelatoriosFinanceiros({ inicio, fim }: Props) {
  const clientesBrutos = useLista(() => clienteApi.listar());
  const fornecedoresBrutos = useLista(() => fornecedorApi.listar());
  const categoriasDespesaBrutas = useLista(() => categoriaDespesaApi.listar());
  const categoriasCaixaBrutas = useLista(() => categoriaFluxoCaixaApi.listar());

  const clientes = useMemo(() => opcoesDeClientes(clientesBrutos), [clientesBrutos]);
  const fornecedores = useMemo<OpcaoSelecao[]>(
    () => fornecedoresBrutos.map((fornecedor) => ({ valor: fornecedor.id, rotulo: fornecedor.nome })),
    [fornecedoresBrutos],
  );
  const categoriasDespesa = useMemo<Opcao[]>(
    () => categoriasDespesaBrutas.map((categoria) => ({ valor: categoria.id, rotulo: categoria.nome })),
    [categoriasDespesaBrutas],
  );
  const categoriasCaixa = useMemo<Opcao[]>(
    () => categoriasCaixaBrutas.map((categoria) => ({ valor: categoria.id, rotulo: categoria.nome })),
    [categoriasCaixaBrutas],
  );

  return (
    <>
      <SecaoFluxo inicio={inicio} fim={fim} categorias={categoriasCaixa} />
      <SecaoContas tipo="RECEBER" inicio={inicio} fim={fim} parceiros={clientes} categorias={[]} />
      <SecaoContas tipo="PAGAR" inicio={inicio} fim={fim} parceiros={fornecedores} categorias={categoriasDespesa} />
      <SecaoVencimentos inicio={inicio} fim={fim} />
    </>
  );
}
