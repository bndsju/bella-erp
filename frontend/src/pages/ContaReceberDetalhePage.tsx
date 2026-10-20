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
import { CancelarContaReceberDialog } from "@/components/contasreceber/CancelarContaReceberDialog";
import { DadosBasicosContaReceberDialog } from "@/components/contasreceber/DadosBasicosContaReceberDialog";
import { RegistrarRecebimentoDialog } from "@/components/contasreceber/RegistrarRecebimentoDialog";
import { StatusContaReceberBadge } from "@/components/contasreceber/StatusContaReceberBadge";
import { contaReceberApi } from "@/lib/contaReceberApi";
import { ApiError } from "@/lib/api";
import { formatarData, formatarMoeda } from "@/lib/formatos";
import type { ContaReceberResponse, ParcelaContaReceberResponse, StatusParcela } from "@/types/contaReceber";

const ROTULO_PARCELA: Record<StatusParcela, string> = {
  PENDENTE: "Pendente",
  PARCIALMENTE_RECEBIDA: "Parcialmente recebida",
  RECEBIDA: "Recebida",
  CANCELADA: "Cancelada",
};

export function ContaReceberDetalhePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [conta, setConta] = useState<ContaReceberResponse | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [parcelaEmRecebimento, setParcelaEmRecebimento] = useState<ParcelaContaReceberResponse | null>(null);
  const [dialogDadosAberto, setDialogDadosAberto] = useState(false);
  const [dialogCancelarAberto, setDialogCancelarAberto] = useState(false);
  const [cancelando, setCancelando] = useState(false);

  useEffect(() => {
    if (!id) return;
    contaReceberApi
      .buscarPorId(id)
      .then(setConta)
      .catch(() => {
        toast.error("Não foi possível carregar a conta");
        navigate("/contas-receber");
      })
      .finally(() => setCarregando(false));
  }, [id, navigate]);

  async function cancelar(motivo: string) {
    if (!id) return;
    setCancelando(true);
    try {
      setConta(await contaReceberApi.cancelar(id, motivo));
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
        <CardContent className="space-y-4 pt-6">
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
        </CardContent>
      </Card>
    );
  }

  const cancelada = conta.status === "CANCELADA";
  const temRecebimentos = conta.parcelas.some((parcela) => parcela.recebimentos.length > 0);
  const podeRegistrarRecebimento = !cancelada;

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <h1 className="text-xl font-medium">{conta.descricao}</h1>
          <StatusContaReceberBadge status={conta.status} vencida={conta.vencida} />
        </div>
        <div className="flex flex-wrap gap-2">
          <Button variant="outline" asChild>
            <Link to="/contas-receber">Voltar</Link>
          </Button>
          {!cancelada && (
            <Button variant="outline" onClick={() => setDialogDadosAberto(true)}>
              Editar dados
            </Button>
          )}
          {!cancelada && !temRecebimentos && (
            <>
              <Button variant="outline" asChild>
                <Link to={`/contas-receber/${conta.id}/editar`}>Editar valor e parcelas</Link>
              </Button>
              <Button variant="outline" onClick={() => setDialogCancelarAberto(true)}>
                Cancelar conta
              </Button>
            </>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">Valor total</p>
            <p className="text-lg font-medium">{formatarMoeda(conta.valorTotal)}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">Recebido</p>
            <p className="text-lg font-medium text-primary">{formatarMoeda(conta.valorRecebido)}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-6">
            <p className="text-xs text-muted-foreground">Saldo pendente</p>
            <p className="text-lg font-medium">{formatarMoeda(conta.saldoPendente)}</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardContent className="grid grid-cols-2 gap-4 pt-6 text-sm sm:grid-cols-4">
          <div>
            <p className="text-muted-foreground">Cliente</p>
            <p className="font-medium">{conta.clienteNome ?? "—"}</p>
          </div>
          <div>
            <p className="text-muted-foreground">Data de lançamento</p>
            <p className="font-medium">{formatarData(conta.dataLancamento)}</p>
          </div>
          <div>
            <p className="text-muted-foreground">Origem</p>
            <p className="font-medium">{conta.origem === "MANUAL" ? "Lançamento manual" : "Venda"}</p>
          </div>
          <div>
            <p className="text-muted-foreground">Parcelas</p>
            <p className="font-medium">{conta.parcelas.length}</p>
          </div>
          {conta.observacoes && (
            <div className="col-span-2 sm:col-span-4">
              <p className="text-muted-foreground">Observações</p>
              <p className="font-medium">{conta.observacoes}</p>
            </div>
          )}
          {cancelada && (
            <div className="col-span-2 sm:col-span-4">
              <p className="text-muted-foreground">Motivo do cancelamento</p>
              <p className="font-medium">{conta.motivoCancelamento}</p>
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Parcelas e recebimentos</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="rounded-lg border border-border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Parcela</TableHead>
                  <TableHead>Vencimento</TableHead>
                  <TableHead className="text-right">Valor</TableHead>
                  <TableHead className="text-right">Recebido</TableHead>
                  <TableHead className="text-right">Saldo</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead className="text-right">Ações</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {conta.parcelas.map((parcela) => (
                  <Fragment key={parcela.id}>
                    <TableRow>
                      <TableCell className="font-medium">{parcela.numero}ª</TableCell>
                      <TableCell>{formatarData(parcela.vencimento)}</TableCell>
                      <TableCell className="text-right">{formatarMoeda(parcela.valor)}</TableCell>
                      <TableCell className="text-right">{formatarMoeda(parcela.valorRecebido)}</TableCell>
                      <TableCell className="text-right">{formatarMoeda(parcela.saldoPendente)}</TableCell>
                      <TableCell>
                        <span className="inline-flex gap-1">
                          <Badge variant={parcela.status === "RECEBIDA" ? "default" : "outline"}>
                            {ROTULO_PARCELA[parcela.status]}
                          </Badge>
                          {parcela.vencida && <Badge variant="destructive">Vencida</Badge>}
                        </span>
                      </TableCell>
                      <TableCell className="text-right">
                        {podeRegistrarRecebimento && parcela.saldoPendente > 0 && (
                          <Button variant="ghost" size="sm" onClick={() => setParcelaEmRecebimento(parcela)}>
                            Registrar recebimento
                          </Button>
                        )}
                      </TableCell>
                    </TableRow>
                    {parcela.recebimentos.map((recebimento) => (
                      <TableRow key={recebimento.id} className="bg-muted/30 text-xs text-muted-foreground">
                        <TableCell />
                        <TableCell>{formatarData(recebimento.dataRecebimento)}</TableCell>
                        <TableCell colSpan={4}>
                          Recebido {formatarMoeda(recebimento.valorRecebido)}
                          {recebimento.juros > 0 && ` + juros ${formatarMoeda(recebimento.juros)}`}
                          {recebimento.multa > 0 && ` + multa ${formatarMoeda(recebimento.multa)}`}
                          {recebimento.desconto > 0 && ` − desconto ${formatarMoeda(recebimento.desconto)}`}
                          {" · "}em caixa {formatarMoeda(recebimento.valorTotalRecebido)}
                          {recebimento.observacao && ` · ${recebimento.observacao}`}
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

      <RegistrarRecebimentoDialog
        open={parcelaEmRecebimento !== null}
        onOpenChange={(aberto) => !aberto && setParcelaEmRecebimento(null)}
        contaId={conta.id}
        parcela={parcelaEmRecebimento}
        onSalvo={setConta}
      />
      <DadosBasicosContaReceberDialog
        open={dialogDadosAberto}
        onOpenChange={setDialogDadosAberto}
        conta={conta}
        onSalvo={setConta}
      />
      <CancelarContaReceberDialog
        open={dialogCancelarAberto}
        onOpenChange={setDialogCancelarAberto}
        processando={cancelando}
        onConfirmar={cancelar}
      />
    </div>
  );
}
