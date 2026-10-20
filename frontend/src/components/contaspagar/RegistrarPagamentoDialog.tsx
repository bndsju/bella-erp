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
import { contaPagarApi } from "@/lib/contaPagarApi";
import { criarPagamentoSchema, type PagamentoFormValues } from "@/lib/contaPagarSchema";
import { ApiError } from "@/lib/api";
import { dataDeHoje, formatarData, formatarMoeda } from "@/lib/formatos";
import type { ContaPagarResponse, ParcelaContaPagarResponse } from "@/types/contaPagar";

interface RegistrarPagamentoDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  contaId: string;
  parcela: ParcelaContaPagarResponse | null;
  onSalvo: (conta: ContaPagarResponse) => void;
}

export function RegistrarPagamentoDialog({
  open,
  onOpenChange,
  contaId,
  parcela,
  onSalvo,
}: RegistrarPagamentoDialogProps) {
  const saldo = parcela?.saldoPendente ?? 0;
  const resolver = useMemo(() => zodResolver(criarPagamentoSchema(saldo)), [saldo]);

  const form = useForm<PagamentoFormValues>({
    resolver,
    defaultValues: { valorPago: saldo, dataPagamento: dataDeHoje(), juros: 0, multa: 0, desconto: 0, observacao: "" },
  });

  useEffect(() => {
    if (open) {
      form.reset({
        valorPago: saldo,
        dataPagamento: dataDeHoje(),
        juros: 0,
        multa: 0,
        desconto: 0,
        observacao: "",
      });
    }
  }, [open, saldo, form]);

  const valorPago = form.watch("valorPago") || 0;
  const juros = form.watch("juros") || 0;
  const multa = form.watch("multa") || 0;
  const desconto = form.watch("desconto") || 0;
  const saidaDoCaixa = valorPago + juros + multa;
  const saldoRestante = saldo - valorPago - desconto;

  async function onSubmit(dados: PagamentoFormValues) {
    if (!parcela) return;
    try {
      const atualizada = await contaPagarApi.registrarPagamento(contaId, {
        parcelaId: parcela.id,
        valorPago: dados.valorPago,
        dataPagamento: dados.dataPagamento,
        juros: dados.juros,
        multa: dados.multa,
        desconto: dados.desconto,
        observacao: dados.observacao || undefined,
      });
      toast.success("Pagamento registrado");
      onOpenChange(false);
      onSalvo(atualizada);
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Erro ao registrar o pagamento", {
        description: erro instanceof ApiError ? erro.erros?.join(", ") : undefined,
      });
    }
  }

  const camposNumericos = [
    { nome: "valorPago", rotulo: "Valor pago" },
    { nome: "juros", rotulo: "Juros" },
    { nome: "multa", rotulo: "Multa" },
    { nome: "desconto", rotulo: "Desconto" },
  ] as const;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>Registrar pagamento</DialogTitle>
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
              name="dataPagamento"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Data do pagamento</FormLabel>
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
                <p className="text-xs text-muted-foreground">Sai do caixa (valor + juros + multa)</p>
                <p className="font-medium">{formatarMoeda(saidaDoCaixa)}</p>
              </div>
              <div>
                <p className="text-xs text-muted-foreground">Saldo da parcela após o pagamento</p>
                <p className="font-medium">{formatarMoeda(Math.max(saldoRestante, 0))}</p>
              </div>
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                Cancelar
              </Button>
              <Button type="submit" disabled={form.formState.isSubmitting}>
                {form.formState.isSubmitting ? "Registrando..." : "Registrar pagamento"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
