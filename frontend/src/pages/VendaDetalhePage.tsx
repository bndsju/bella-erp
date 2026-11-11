import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { VendaStatusBadge } from "@/components/vendas/VendaStatusBadge";
import { vendaApi } from "@/lib/vendaApi";
import { ApiError } from "@/lib/api";
import { labelFormaPagamento } from "@/lib/formaPagamento";
import { VendaResumoValores } from "@/components/vendas/VendaResumoValores";
import type { VendaResponse } from "@/types/venda";
import { HistoricoAlteracoes } from "@/components/auditoria/HistoricoAlteracoes";
import { CabecalhoPagina } from "@/components/layout/CabecalhoPagina";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

export function VendaDetalhePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [carregando, setCarregando] = useState(true);
  const [venda, setVenda] = useState<VendaResponse | null>(null);
  const [processandoAcao, setProcessandoAcao] = useState(false);

  useEffect(() => {
    if (!id) return;
    vendaApi
      .buscarPorId(id)
      .then(setVenda)
      .catch(() => {
        toast.error("Não foi possível carregar a venda");
        navigate("/vendas");
      })
      .finally(() => setCarregando(false));
  }, [id, navigate]);

  async function cancelar() {
    if (!id) return;
    setProcessandoAcao(true);
    try {
      const atualizada = await vendaApi.cancelar(id);
      setVenda(atualizada);
      toast.success("Venda cancelada");
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Não foi possível cancelar a venda");
    } finally {
      setProcessandoAcao(false);
    }
  }

  if (carregando) {
    return (
      <>
        <CabecalhoPagina titulo="Venda" />
        <Card>
          <div className="space-y-4">
            <Skeleton className="h-11 w-full" />
            <Skeleton className="h-11 w-full" />
            <Skeleton className="h-11 w-full" />
          </div>
        </Card>
      </>
    );
  }

  if (!venda) return null;

  return (
    <>
      <CabecalhoPagina titulo="Venda" acao={<VendaStatusBadge status={venda.status} />} />

      <Card>
        <CardTitle>{venda.clienteNome}</CardTitle>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Produto</TableHead>
              <TableHead>Quantidade</TableHead>
              <TableHead>Valor unitário</TableHead>
              <TableHead className="text-right">Subtotal</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {venda.itens.map((item) => (
              <TableRow key={item.id}>
                <TableCell className="tabela__forte">
                  {item.produtoCodigoInterno} — {item.produtoNome}
                </TableCell>
                <TableCell>{item.quantidade}</TableCell>
                <TableCell>{formatoMoeda.format(item.valorUnitario)}</TableCell>
                <TableCell className="text-right">{formatoMoeda.format(item.subtotal)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Card>

      <VendaResumoValores
        subtotal={venda.subtotal}
        valorDesconto={venda.valorDesconto}
        valorFrete={venda.valorFrete}
        valorTotal={venda.valorTotal}
      />

      <Card>
        <CardTitle>Pagamento</CardTitle>
        <div className="linha">
          <span>Forma de pagamento</span>
          <span>{labelFormaPagamento(venda.formaPagamento)}</span>
        </div>
        <div className="linha">
          <span>Condição de pagamento</span>
          <span>{venda.condicaoPagamento}</span>
        </div>
      </Card>

      {venda.status === "CONCLUIDA" && (
        <div className="acoes">
          <Button type="button" variant="secondary" disabled={processandoAcao} onClick={cancelar}>
            Cancelar venda
          </Button>
        </div>
      )}

      <HistoricoAlteracoes modulo="VENDA" entidadeId={venda.id} atualizarCom={venda.status} />
    </>
  );
}
