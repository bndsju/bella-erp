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
import { fornecedorApi } from "@/lib/fornecedorApi";
import { ApiError } from "@/lib/api";
import { fornecedorSchema, type FornecedorFormValues } from "@/lib/fornecedorSchema";
import type { FornecedorResponse } from "@/types/fornecedor";

interface FornecedorFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  fornecedor: FornecedorResponse | null;
  onSalvo: () => void;
}

export function FornecedorFormDialog({
  open,
  onOpenChange,
  fornecedor,
  onSalvo,
}: FornecedorFormDialogProps) {
  const form = useForm<FornecedorFormValues>({
    resolver: zodResolver(fornecedorSchema),
    defaultValues: { nome: "", telefone: "" },
  });

  useEffect(() => {
    if (open) {
      form.reset({
        nome: fornecedor?.nome ?? "",
        telefone: fornecedor?.telefone ?? "",
      });
    }
  }, [open, fornecedor, form]);

  async function onSubmit(dados: FornecedorFormValues) {
    try {
      if (fornecedor) {
        await fornecedorApi.editar(fornecedor.id, dados);
        toast.success("Fornecedor atualizado");
      } else {
        await fornecedorApi.cadastrar(dados);
        toast.success("Fornecedor cadastrado");
      }
      onOpenChange(false);
      onSalvo();
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Erro ao salvar fornecedor");
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{fornecedor ? "Editar fornecedor" : "Novo fornecedor"}</DialogTitle>
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
                    <Input placeholder="Fornecedor Alfa Ltda" {...field} />
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
