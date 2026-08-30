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
import { unidadeMedidaApi } from "@/lib/unidadeMedidaApi";
import { ApiError } from "@/lib/api";
import { unidadeMedidaSchema, type UnidadeMedidaFormValues } from "@/lib/produtoSchema";
import type { UnidadeMedidaResponse } from "@/types/unidadeMedida";

interface UnidadeMedidaFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  unidadeMedida: UnidadeMedidaResponse | null;
  onSalvo: () => void;
}

export function UnidadeMedidaFormDialog({
  open,
  onOpenChange,
  unidadeMedida,
  onSalvo,
}: UnidadeMedidaFormDialogProps) {
  const form = useForm<UnidadeMedidaFormValues>({
    resolver: zodResolver(unidadeMedidaSchema),
    defaultValues: { nome: "", sigla: "" },
  });

  useEffect(() => {
    if (open) {
      form.reset({ nome: unidadeMedida?.nome ?? "", sigla: unidadeMedida?.sigla ?? "" });
    }
  }, [open, unidadeMedida, form]);

  async function onSubmit(dados: UnidadeMedidaFormValues) {
    try {
      if (unidadeMedida) {
        await unidadeMedidaApi.editar(unidadeMedida.id, dados);
        toast.success("Unidade de medida atualizada");
      } else {
        await unidadeMedidaApi.cadastrar(dados);
        toast.success("Unidade de medida cadastrada");
      }
      onOpenChange(false);
      onSalvo();
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Erro ao salvar unidade de medida");
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{unidadeMedida ? "Editar unidade de medida" : "Nova unidade de medida"}</DialogTitle>
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
                    <Input placeholder="Quilograma" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="sigla"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Sigla</FormLabel>
                  <FormControl>
                    <Input placeholder="kg" {...field} />
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
