import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { dataDeHoje, formatarMoeda } from "@/lib/formatos";
import type { MovimentacaoCaixaResponse } from "@/types/fluxoCaixa";

interface RealizarMovimentacaoCaixaDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  movimentacao: MovimentacaoCaixaResponse | null;
  processando: boolean;
  onConfirmar: (dataEfetiva: string) => void;
}

export function RealizarMovimentacaoCaixaDialog({
  open,
  onOpenChange,
  movimentacao,
  processando,
  onConfirmar,
}: RealizarMovimentacaoCaixaDialogProps) {
  const [data, setData] = useState(dataDeHoje());

  useEffect(() => {
    if (open) setData(dataDeHoje());
  }, [open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Realizar lançamento</DialogTitle>
          <DialogDescription>
            {movimentacao
              ? `${movimentacao.descricao} — ${formatarMoeda(movimentacao.valor)}. O mesmo lançamento passa de previsto para realizado, sem duplicar o valor.`
              : ""}
          </DialogDescription>
        </DialogHeader>
        <div className="campo">
          <label>Data de realização</label>
          <Input type="date" max={dataDeHoje()} value={data} onChange={(e) => setData(e.target.value)} />
        </div>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Voltar
          </Button>
          <Button type="button" disabled={!data || processando} onClick={() => onConfirmar(data)}>
            Realizar
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
