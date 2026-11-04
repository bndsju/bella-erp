import { Card, CardTitle } from "@/components/ui/card";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

interface OrcamentoResumoValoresProps {
  subtotal: number;
  valorDesconto: number;
  valorFrete: number;
  valorTotal: number;
}

export function OrcamentoResumoValores({
  subtotal,
  valorDesconto,
  valorFrete,
  valorTotal,
}: OrcamentoResumoValoresProps) {
  return (
    <Card>
      <CardTitle>Resumo</CardTitle>
      <div className="linha">
        <span>Subtotal</span>
        <span>{formatoMoeda.format(subtotal)}</span>
      </div>
      <div className="linha">
        <span>Desconto</span>
        <span>- {formatoMoeda.format(valorDesconto)}</span>
      </div>
      <div className="linha">
        <span>Frete</span>
        <span>{formatoMoeda.format(valorFrete)}</span>
      </div>
      <div className="linha">
        <strong>Total</strong>
        <strong>{formatoMoeda.format(valorTotal)}</strong>
      </div>
    </Card>
  );
}
