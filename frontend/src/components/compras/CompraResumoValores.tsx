import { Card, CardTitle } from "@/components/ui/card";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

interface CompraResumoValoresProps {
  valorProdutos: number;
  descontoTotal: number;
  frete: number;
  outrasDespesas: number;
  valorTotal: number;
}

export function CompraResumoValores({
  valorProdutos,
  descontoTotal,
  frete,
  outrasDespesas,
  valorTotal,
}: CompraResumoValoresProps) {
  return (
    <Card>
      <CardTitle>Resumo</CardTitle>
      <div className="linha">
        <span>Produtos</span>
        <span>{formatoMoeda.format(valorProdutos)}</span>
      </div>
      <div className="linha">
        <span>Descontos</span>
        <span>- {formatoMoeda.format(descontoTotal)}</span>
      </div>
      <div className="linha">
        <span>Frete</span>
        <span>{formatoMoeda.format(frete)}</span>
      </div>
      <div className="linha">
        <span>Outras despesas</span>
        <span>{formatoMoeda.format(outrasDespesas)}</span>
      </div>
      <div className="linha">
        <strong>Total</strong>
        <strong>{formatoMoeda.format(valorTotal)}</strong>
      </div>
    </Card>
  );
}
