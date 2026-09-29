import { useEffect } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
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
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { transportadoraApi } from "@/lib/transportadoraApi";
import { ApiError } from "@/lib/api";
import { transportadoraSchema, type TransportadoraFormValues } from "@/lib/transportadoraSchema";
import type { TransportadoraResponse } from "@/types/transportadora";

interface TransportadoraFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  transportadora: TransportadoraResponse | null;
  onSalvo: () => void;
}

export function TransportadoraFormDialog({
  open,
  onOpenChange,
  transportadora,
  onSalvo,
}: TransportadoraFormDialogProps) {
  const form = useForm<TransportadoraFormValues>({
    resolver: zodResolver(transportadoraSchema),
    defaultValues: { nome: "", telefone: "" },
  });

  useEffect(() => {
    if (open) {
      form.reset({
        nome: transportadora?.nome ?? "",
        telefone: transportadora?.telefone ?? "",
      });
    }
  }, [open, transportadora, form]);

  async function onSubmit(dados: TransportadoraFormValues) {
    try {
      if (transportadora) {
        await transportadoraApi.editar(transportadora.id, dados);
        toast.success("Transportadora atualizada");
      } else {
        await transportadoraApi.cadastrar(dados);
        toast.success("Transportadora cadastrada");
      }
      onOpenChange(false);
      onSalvo();
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Erro ao salvar transportadora");
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{transportadora ? "Editar transportadora" : "Nova transportadora"}</DialogTitle>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormField
              control={form.control}
              name="nome"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Nome</FormLabel>
                  <FormControl>
                    <Input placeholder="Transportadora Rápida Ltda" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="telefone"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Telefone</FormLabel>
                  <FormControl>
                    <Input placeholder="(11) 4002-8922" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                Cancelar
              </Button>
              <Button type="submit" disabled={form.formState.isSubmitting}>
                Salvar
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}
