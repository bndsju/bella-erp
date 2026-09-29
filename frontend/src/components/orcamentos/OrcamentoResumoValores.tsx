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
    <div className="grid grid-cols-2 gap-3 rounded-lg border border-border bg-muted/40 p-4 sm:grid-cols-4">
      <div>
        <p className="text-xs text-muted-foreground">Subtotal</p>
        <p className="font-medium">{formatoMoeda.format(subtotal)}</p>
      </div>
      <div>
        <p className="text-xs text-muted-foreground">Desconto</p>
        <p className="font-medium">- {formatoMoeda.format(valorDesconto)}</p>
      </div>
      <div>
        <p className="text-xs text-muted-foreground">Frete</p>
        <p className="font-medium">{formatoMoeda.format(valorFrete)}</p>
      </div>
      <div>
        <p className="text-xs text-muted-foreground">Total</p>
        <p className="text-lg font-medium text-primary">{formatoMoeda.format(valorTotal)}</p>
      </div>
    </div>
  );
}
