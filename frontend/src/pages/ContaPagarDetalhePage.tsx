import { Fragment, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { CancelarContaPagarDialog } from "@/components/contaspagar/CancelarContaPagarDialog";
import { DadosBasicosContaPagarDialog } from "@/components/contaspagar/DadosBasicosContaPagarDialog";
import { RegistrarPagamentoDialog } from "@/components/contaspagar/RegistrarPagamentoDialog";
import { StatusContaPagarBadge } from "@/components/contaspagar/StatusContaPagarBadge";
import { contaPagarApi } from "@/lib/contaPagarApi";
import { ApiError } from "@/lib/api";
import { formatarData, formatarMoeda } from "@/lib/formatos";
import type { ContaPagarResponse, ParcelaContaPagarResponse, StatusParcela } from "@/types/contaPagar";
import { HandCoins } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

const ROTULO_PARCELA: Record<StatusParcela, string> = {
  PENDENTE: "Pendente",
  PARCIALMENTE_PAGA: "Parcialmente paga",
  PAGA: "Paga",
  CANCELADA: "Cancelada",
};

export function ContaPagarDetalhePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [conta, setConta] = useState<ContaPagarResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [parcelaEmPagamento, setParcelaEmPagamento] = useState<ParcelaContaPagarResponse | null>(null);
  const [dialogDadosAberto, setDialogDadosAberto] = useState(false);
  const [dialogCancelarAberto, setDialogCancelarAberto] = useState(false);
  const [cancelando, setCancelando] = useState(false);

  useEffect(() => {
    if (!id) return;
    contaPagarApi
      .buscarPorId(id)
      .then(setConta)
      .catch(() => {
        toast.error("Não foi possível carregar a conta");
        navigate("/contas-pagar");
      })
      .finally(() => setCarregando(false));
  }, [id, navigate]);

  async function cancelar(motivo: string) {
    if (!id) return;
    setCancelando(true);
    try {
      setConta(await contaPagarApi.cancelar(id, motivo));
      setDialogCancelarAberto(false);
      toast.success("Conta cancelada");
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Não foi possível cancelar a conta");
    } finally {
      setCancelando(false);
    }
  }

  if (carregando || !conta) {
    return (
      <Card>
        <CardContent className="space-y-4">
          <Skeleton className="h-11 w-full" />
          <Skeleton className="h-11 w-full" />
          <Skeleton className="h-11 w-full" />
        </CardContent>
      </Card>
    );
  }

  const cancelada = conta.status === "CANCELADA";
  const temPagamentos = conta.parcelas.some((parcela) => parcela.pagamentos.length > 0);
  const podeRegistrarPagamento = !cancelada;

  return (
    <>
      <CabecalhoPagina
        titulo={conta.descricao}
        acao={
          <>
            <StatusContaPagarBadge status={conta.status} vencida={conta.vencida} />
            <Button variant="outline" asChild>
              <Link to="/contas-pagar">Voltar</Link>
            </Button>
            {!cancelada && (
              <Button variant="outline" onClick={() => setDialogDadosAberto(true)}>
                Editar dados
              </Button>
            )}
            {!cancelada && !temPagamentos && conta.origem === "MANUAL" && (
              <Button variant="outline" asChild>
                <Link to={`/contas-pagar/${conta.id}/editar`}>Editar valor e parcelas</Link>
              </Button>
            )}
            {!cancelada && !temPagamentos && (
              <Button variant="outline" onClick={() => setDialogCancelarAberto(true)}>
                Cancelar conta
              </Button>
            )}
          </>
        }
      />

      <div className="grade grade--3">
        <Card className="indicador">
          <div className="indicador__rotulo">Valor total</div>
          <div className="indicador__valor">{formatarMoeda(conta.valorTotal)}</div>
        </Card>
        <Card className="indicador">
          <div className="indicador__rotulo">Pago</div>
          <div className="indicador__valor">{formatarMoeda(conta.valorPago)}</div>
        </Card>
        <Card className="indicador">
          <div className="indicador__rotulo">Saldo pendente</div>
          <div className="indicador__valor">{formatarMoeda(conta.saldoPendente)}</div>
        </Card>
      </div>

      <Card>
        <CardTitle>Dados</CardTitle>
        <div>
          <div className="linha">
            <span>Fornecedor</span>
            <span>{conta.fornecedorNome ?? "—"}</span>
          </div>
          <div className="linha">
            <span>Categoria</span>
            <span>{conta.categoriaDespesaNome}</span>
          </div>
          <div className="linha">
            <span>Data de lançamento</span>
            <span>{formatarData(conta.dataLancamento)}</span>
          </div>
          <div className="linha">
            <span>Origem</span>
            <span>{conta.origem === "MANUAL" ? "Lançamento manual" : "Compra"}</span>
          </div>
          <div className="linha">
            <span>Parcelas</span>
            <span>{conta.parcelas.length}</span>
          </div>
          {conta.observacoes && (
            <div className="linha">
            <span>Observações</span>
            <span>{conta.observacoes}</span>
          </div>
          )}
          {cancelada && (
            <div className="linha">
            <span>Motivo do cancelamento</span>
            <span>{conta.motivoCancelamento}</span>
          </div>
          )}
        </div>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Parcelas e pagamentos</CardTitle>
        </CardHeader>
        <CardContent>
          <div>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Parcela</TableHead>
                  <TableHead>Vencimento</TableHead>
                  <TableHead className="text-right">Valor</TableHead>
                  <TableHead className="text-right">Pago</TableHead>
                  <TableHead className="text-right">Saldo</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Ações</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {conta.parcelas.map((parcela) => (
                  <Fragment key={parcela.id}>
                    <TableRow>
                      <TableCell className="tabela__forte">{parcela.numero}ª</TableCell>
                      <TableCell>{formatarData(parcela.vencimento)}</TableCell>
                      <TableCell className="text-right">{formatarMoeda(parcela.valor)}</TableCell>
                      <TableCell className="text-right">{formatarMoeda(parcela.valorPago)}</TableCell>
                      <TableCell className="text-right">{formatarMoeda(parcela.saldoPendente)}</TableCell>
                      <TableCell>
                        <span className="inline-flex gap-1">
                          <Badge variant={parcela.status === "PAGA" ? "default" : "outline"}>
                            {ROTULO_PARCELA[parcela.status]}
                          </Badge>
                          {parcela.vencida && <Badge variant="destructive">Vencida</Badge>}
                        </span>
                      </TableCell>
                      <TableCell className="text-right">
                        <MenuAcoes
                          itens={[
                            {
                              rotulo: "Registrar pagamento",
                              icone: HandCoins,
                              oculto: !(podeRegistrarPagamento && parcela.saldoPendente > 0),
                              aoClicar: () => setParcelaEmPagamento(parcela),
                            },
                          ]}
                        />
                      </TableCell>
                    </TableRow>
                    {parcela.pagamentos.map((pagamento) => (
                      <TableRow key={pagamento.id} className="bg-muted/30 text-xs text-muted-foreground">
                        <TableCell />
                        <TableCell>{formatarData(pagamento.dataPagamento)}</TableCell>
                        <TableCell colSpan={4}>
                          Pago {formatarMoeda(pagamento.valorPago)}
                          {pagamento.juros > 0 && ` + juros ${formatarMoeda(pagamento.juros)}`}
                          {pagamento.multa > 0 && ` + multa ${formatarMoeda(pagamento.multa)}`}
                          {pagamento.desconto > 0 && ` − desconto ${formatarMoeda(pagamento.desconto)}`}
                          {" · "}saiu do caixa {formatarMoeda(pagamento.valorDesembolsado)}
                          {pagamento.observacao && ` · ${pagamento.observacao}`}
                        </TableCell>
                        <TableCell />
                      </TableRow>
                    ))}
                  </Fragment>
                ))}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>

      <RegistrarPagamentoDialog
        open={parcelaEmPagamento !== null}
        onOpenChange={(aberto) => !aberto && setParcelaEmPagamento(null)}
        contaId={conta.id}
        parcela={parcelaEmPagamento}
        onSalvo={setConta}
      />
      <DadosBasicosContaPagarDialog
        open={dialogDadosAberto}
        onOpenChange={setDialogDadosAberto}
        conta={conta}
        onSalvo={setConta}
      />
      <CancelarContaPagarDialog
        open={dialogCancelarAberto}
        onOpenChange={setDialogCancelarAberto}
        processando={cancelando}
        onConfirmar={cancelar}
      />
    </>
  );
}
