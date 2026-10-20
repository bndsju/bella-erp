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

interface ReceberCompraDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  processando: boolean;
  onConfirmar: (dataRecebimento: string) => void;
}

function hoje(): string {
  const agora = new Date();
  const mes = String(agora.getMonth() + 1).padStart(2, "0");
  const dia = String(agora.getDate()).padStart(2, "0");
  return `${agora.getFullYear()}-${mes}-${dia}`;
}

export function ReceberCompraDialog({ open, onOpenChange, processando, onConfirmar }: ReceberCompraDialogProps) {
  const [data, setData] = useState(hoje());

  useEffect(() => {
    if (open) setData(hoje());
  }, [open]);

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Receber compra</DialogTitle>
          <DialogDescription>
            O recebimento gera uma entrada no estoque para cada item e só pode ser feito uma vez.
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-2">
          <label className="text-sm font-medium">Data do recebimento</label>
          <Input type="date" max={hoje()} value={data} onChange={(e) => setData(e.target.value)} />
        </div>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Voltar
          </Button>
          <Button type="button" disabled={!data || processando} onClick={() => onConfirmar(data)}>
            Confirmar recebimento
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
