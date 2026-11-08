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
import { HandCoins } from "lucide-react";
import { MenuAcoes } from "@/components/ui/menu-acoes";
import { HistoricoAlteracoes } from "@/components/auditoria/HistoricoAlteracoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

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
        <CardContent className="space-y-4">
          <Skeleton className="h-11 w-full" />
          <Skeleton className="h-11 w-full" />
          <Skeleton className="h-11 w-full" />
        </CardContent>
      </Card>
    );
  }

  const cancelada = conta.status === "CANCELADA";
  const temRecebimentos = conta.parcelas.some((parcela) => parcela.recebimentos.length > 0);
  const podeRegistrarRecebimento = !cancelada;

  return (
    <>
      <CabecalhoPagina
        titulo={conta.descricao}
        acao={
          <>
            <StatusContaReceberBadge status={conta.status} vencida={conta.vencida} />
            <Button variant="outline" asChild>
              <Link to="/contas-receber">Voltar</Link>
            </Button>
            {!cancelada && (
              <Button variant="outline" onClick={() => setDialogDadosAberto(true)}>
                Editar dados
              </Button>
            )}
            {!cancelada && !temRecebimentos && conta.origem === "MANUAL" && (
              <Button variant="outline" asChild>
                <Link to={`/contas-receber/${conta.id}/editar`}>Editar valor e parcelas</Link>
              </Button>
            )}
            {!cancelada && !temRecebimentos && (
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
          <div className="indicador__rotulo">Recebido</div>
          <div className="indicador__valor">{formatarMoeda(conta.valorRecebido)}</div>
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
            <span>Cliente</span>
            <span>{conta.clienteNome ?? "—"}</span>
          </div>
          <div className="linha">
            <span>Data de lançamento</span>
            <span>{formatarData(conta.dataLancamento)}</span>
          </div>
          <div className="linha">
            <span>Origem</span>
            <span>{conta.origem === "MANUAL" ? "Lançamento manual" : "Venda"}</span>
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
          <CardTitle>Parcelas e recebimentos</CardTitle>
        </CardHeader>
        <CardContent>
          <div>
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
                      <TableCell className="tabela__forte">{parcela.numero}ª</TableCell>
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
                        <MenuAcoes
                          itens={[
                            {
                              rotulo: "Registrar recebimento",
                              icone: HandCoins,
                              oculto: !(podeRegistrarRecebimento && parcela.saldoPendente > 0),
                              aoClicar: () => setParcelaEmRecebimento(parcela),
                            },
                          ]}
                        />
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

      <HistoricoAlteracoes modulo="CONTA_RECEBER" entidadeId={conta.id} atualizarCom={conta} />
    </>
  );
}
