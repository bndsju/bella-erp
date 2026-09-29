import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { VendaStatusBadge } from "@/components/vendas/VendaStatusBadge";
import { vendaApi } from "@/lib/vendaApi";
import { ApiError } from "@/lib/api";
import { labelFormaPagamento } from "@/lib/formaPagamento";
import { VendaResumoValores } from "@/components/vendas/VendaResumoValores";
import type { VendaResponse } from "@/types/venda";

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
      <Card>
        <CardContent className="space-y-4 pt-6">
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
          <Skeleton className="h-9 w-full" />
        </CardContent>
      </Card>
    );
  }

  if (!venda) return null;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-medium">Venda</h1>
        <VendaStatusBadge status={venda.status} />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>{venda.clienteNome}</CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="rounded-lg border border-border">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border text-left text-muted-foreground">
                  <th className="p-3 font-normal">Produto</th>
                  <th className="p-3 font-normal">Quantidade</th>
                  <th className="p-3 font-normal">Valor unitário</th>
                  <th className="p-3 text-right font-normal">Subtotal</th>
                </tr>
              </thead>
              <tbody>
                {venda.itens.map((item) => (
                  <tr key={item.id} className="border-b border-border last:border-0">
                    <td className="p-3">
                      {item.produtoCodigoInterno} — {item.produtoNome}
                    </td>
                    <td className="p-3">{item.quantidade}</td>
                    <td className="p-3">{formatoMoeda.format(item.valorUnitario)}</td>
                    <td className="p-3 text-right">{formatoMoeda.format(item.subtotal)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <VendaResumoValores
            subtotal={venda.subtotal}
            valorDesconto={venda.valorDesconto}
            valorFrete={venda.valorFrete}
            valorTotal={venda.valorTotal}
          />

          <div className="grid grid-cols-2 gap-4 text-sm sm:grid-cols-3">
            <div>
              <p className="text-muted-foreground">Forma de pagamento</p>
              <p className="font-medium">{labelFormaPagamento(venda.formaPagamento)}</p>
            </div>
            <div>
              <p className="text-muted-foreground">Condição de pagamento</p>
              <p className="font-medium">{venda.condicaoPagamento}</p>
            </div>
          </div>

          {venda.status === "CONCLUIDA" && (
            <div className="flex justify-end pt-2">
              <Button type="button" variant="outline" disabled={processandoAcao} onClick={cancelar}>
                Cancelar venda
              </Button>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
