import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { orcamentoApi } from "@/lib/orcamentoApi";
import { transportadoraApi } from "@/lib/transportadoraApi";
import { pedidoApi } from "@/lib/pedidoApi";
import { ApiError } from "@/lib/api";
import type { OrcamentoResponse } from "@/types/orcamento";
import type { TransportadoraResponse } from "@/types/transportadora";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

const SEM_TRANSPORTADORA = "SEM_TRANSPORTADORA";

interface CriarPedidoDeOrcamentoDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function CriarPedidoDeOrcamentoDialog({ open, onOpenChange }: CriarPedidoDeOrcamentoDialogProps) {
  const navigate = useNavigate();
  const [orcamentos, setOrcamentos] = useState<OrcamentoResponse[]>([]);
  const [transportadoras, setTransportadoras] = useState<TransportadoraResponse[]>([]);
  const [orcamentoId, setOrcamentoId] = useState("");
  const [transportadoraId, setTransportadoraId] = useState(SEM_TRANSPORTADORA);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    if (!open) return;
    setOrcamentoId("");
    setTransportadoraId(SEM_TRANSPORTADORA);
    orcamentoApi.listar({ status: "APROVADO" }).then(setOrcamentos).catch(() => {});
    transportadoraApi.listar("ATIVO").then(setTransportadoras).catch(() => {});
  }, [open]);

  async function confirmar() {
    if (!orcamentoId) {
      toast.error("Selecione um orçamento");
      return;
    }
    setEnviando(true);
    try {
      const pedido = await pedidoApi.criarAPartirDeOrcamento({
        orcamentoId,
        transportadoraId: transportadoraId === SEM_TRANSPORTADORA ? undefined : transportadoraId,
      });
      toast.success("Pedido criado a partir do orçamento");
      onOpenChange(false);
      navigate(`/pedidos/${pedido.id}/editar`);
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Não foi possível criar o pedido");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Criar pedido a partir de orçamento</DialogTitle>
        </DialogHeader>
        <div className="space-y-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">Orçamento aprovado</label>
            <Select value={orcamentoId} onValueChange={setOrcamentoId}>
              <SelectTrigger className="w-full">
                <SelectValue placeholder="Selecione o orçamento" />
              </SelectTrigger>
              <SelectContent>
                {orcamentos.length === 0 ? (
                  <div className="px-2 py-1.5 text-sm text-muted-foreground">
                    Nenhum orçamento aprovado disponível
                  </div>
                ) : (
                  orcamentos.map((orcamento) => (
                    <SelectItem key={orcamento.id} value={orcamento.id}>
                      {orcamento.clienteNome} — {formatoMoeda.format(orcamento.valorTotal)}
                    </SelectItem>
                  ))
                )}
              </SelectContent>
            </Select>
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">Transportadora</label>
            <Select value={transportadoraId} onValueChange={setTransportadoraId}>
              <SelectTrigger className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value={SEM_TRANSPORTADORA}>Sem transportadora</SelectItem>
                {transportadoras.map((transportadora) => (
                  <SelectItem key={transportadora.id} value={transportadora.id}>
                    {transportadora.nome}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
            Cancelar
          </Button>
          <Button type="button" disabled={enviando} onClick={confirmar}>
            Criar pedido
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
