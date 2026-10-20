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
    <div className="grid grid-cols-2 gap-3 rounded-lg border border-border bg-muted/40 p-4 sm:grid-cols-5">
      <div>
        <p className="text-xs text-muted-foreground">Produtos</p>
        <p className="font-medium">{formatoMoeda.format(valorProdutos)}</p>
      </div>
      <div>
        <p className="text-xs text-muted-foreground">Descontos</p>
        <p className="font-medium">- {formatoMoeda.format(descontoTotal)}</p>
      </div>
      <div>
        <p className="text-xs text-muted-foreground">Frete</p>
        <p className="font-medium">{formatoMoeda.format(frete)}</p>
      </div>
      <div>
        <p className="text-xs text-muted-foreground">Outras despesas</p>
        <p className="font-medium">{formatoMoeda.format(outrasDespesas)}</p>
      </div>
      <div>
        <p className="text-xs text-muted-foreground">Total</p>
        <p className="text-lg font-medium text-primary">{formatoMoeda.format(valorTotal)}</p>
      </div>
    </div>
  );
}
