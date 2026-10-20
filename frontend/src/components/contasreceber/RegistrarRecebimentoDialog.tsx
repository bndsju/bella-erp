import { useEffect, useMemo } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { contaReceberApi } from "@/lib/contaReceberApi";
import { criarRecebimentoSchema, type RecebimentoFormValues } from "@/lib/contaReceberSchema";
import { ApiError } from "@/lib/api";
import { dataDeHoje, formatarData, formatarMoeda } from "@/lib/formatos";
import type { ContaReceberResponse, ParcelaContaReceberResponse } from "@/types/contaReceber";

interface RegistrarRecebimentoDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  contaId: string;
  parcela: ParcelaContaReceberResponse | null;
  onSalvo: (conta: ContaReceberResponse) => void;
}

export function RegistrarRecebimentoDialog({
  open,
  onOpenChange,
  contaId,
  parcela,
  onSalvo,
}: RegistrarRecebimentoDialogProps) {
  const saldo = parcela?.saldoPendente ?? 0;
  const resolver = useMemo(() => zodResolver(criarRecebimentoSchema(saldo)), [saldo]);

  const form = useForm<RecebimentoFormValues>({
    resolver,
    defaultValues: { valorRecebido: saldo, dataRecebimento: dataDeHoje(), juros: 0, multa: 0, desconto: 0, observacao: "" },
  });

  useEffect(() => {
    if (open) {
      form.reset({
        valorRecebido: saldo,
        dataRecebimento: dataDeHoje(),
        juros: 0,
        multa: 0,
        desconto: 0,
        observacao: "",
      });
    }
  }, [open, saldo, form]);

  const valorRecebido = form.watch("valorRecebido") || 0;
  const juros = form.watch("juros") || 0;
  const multa = form.watch("multa") || 0;
  const desconto = form.watch("desconto") || 0;
  const entradaEmCaixa = valorRecebido + juros + multa;
  const saldoRestante = saldo - valorRecebido - desconto;

  async function onSubmit(dados: RecebimentoFormValues) {
    if (!parcela) return;
    try {
      const atualizada = await contaReceberApi.registrarRecebimento(contaId, {
        parcelaId: parcela.id,
        valorRecebido: dados.valorRecebido,
        dataRecebimento: dados.dataRecebimento,
        juros: dados.juros,
        multa: dados.multa,
        desconto: dados.desconto,
        observacao: dados.observacao || undefined,
      });
      toast.success("Recebimento registrado");
      onOpenChange(false);
      onSalvo(atualizada);
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Erro ao registrar o recebimento", {
        description: erro instanceof ApiError ? erro.erros?.join(", ") : undefined,
      });
    }
  }

  const camposNumericos = [
    { nome: "valorRecebido", rotulo: "Valor recebido" },
    { nome: "juros", rotulo: "Juros" },
    { nome: "multa", rotulo: "Multa" },
    { nome: "desconto", rotulo: "Desconto" },
  ] as const;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Registrar recebimento</DialogTitle>
          {parcela && (
            <DialogDescription>
              Parcela {parcela.numero} — vence em {formatarData(parcela.vencimento)} — saldo de{" "}
              {formatarMoeda(parcela.saldoPendente)}
            </DialogDescription>
          )}
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              {camposNumericos.map(({ nome, rotulo }) => (
                <FormField
                  key={nome}
                  control={form.control}
                  name={nome}
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>{rotulo}</FormLabel>
                      <FormControl>
                        <Input
                          type="number"
                          step="0.01"
                          min="0"
                          {...field}
                          onChange={(e) => field.onChange(e.target.valueAsNumber)}
                        />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              ))}
            </div>
            <FormField
              control={form.control}
              name="dataRecebimento"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Data do recebimento</FormLabel>
                  <FormControl>
                    <Input type="date" max={dataDeHoje()} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="observacao"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Observação</FormLabel>
                  <FormControl>
                    <Textarea rows={2} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="grid grid-cols-2 gap-3 rounded-lg border border-border bg-muted/40 p-3 text-sm">
              <div>
                <p className="text-xs text-muted-foreground">Entra em caixa (valor + juros + multa)</p>
                <p className="font-medium">{formatarMoeda(entradaEmCaixa)}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Saldo da parcela após o recebimento</p>
                <p className="font-medium">{formatarMoeda(Math.max(saldoRestante, 0))}</p>
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                Cancelar
              </Button>
              <Button type="submit" disabled={form.formState.isSubmitting}>
                {form.formState.isSubmitting ? "Registrando..." : "Registrar recebimento"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
