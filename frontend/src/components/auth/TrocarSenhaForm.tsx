import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Form, FormControl, FormDescription, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { ApiError } from "@/lib/api";
import { authApi } from "@/lib/authApi";
import { useAuth } from "@/lib/authContexto";
import {
  alterarMinhaSenhaSchema,
  TAMANHO_MINIMO_SENHA,
  type AlterarMinhaSenhaValues,
} from "@/lib/usuarioSchema";

/** Troca da própria senha (usada em "Minha conta" e na troca obrigatória). Ao terminar, a pessoa entra de novo. */
export function TrocarSenhaForm({ rotuloSenhaAtual = "Senha atual" }: { rotuloSenhaAtual?: string }) {
  const { sair } = useAuth();
  const form = useForm<AlterarMinhaSenhaValues>({
    resolver: zodResolver(alterarMinhaSenhaSchema),
    defaultValues: { senhaAtual: "", novaSenha: "", confirmacao: "" },
  });

  async function onSubmit(dados: AlterarMinhaSenhaValues) {
    try {
      await authApi.alterarSenha(dados.senhaAtual, dados.novaSenha);
      toast.success("Senha alterada. Entre de novo com a senha nova.");
      sair();
    } catch (erro) {
      toast.error(erro instanceof ApiError ? erro.message : "Não foi possível alterar a senha");
    }
  }

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)} className="max-w-sm space-y-4" noValidate>
        <FormField
          control={form.control}
          name="senhaAtual"
          render={({ field }) => (
            <FormItem>
              <FormLabel>{rotuloSenhaAtual}</FormLabel>
              <FormControl>
                <Input type="password" autoComplete="current-password" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="novaSenha"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Nova senha</FormLabel>
              <FormControl>
                <Input type="password" autoComplete="new-password" {...field} />
              </FormControl>
              <FormDescription>Mínimo de {TAMANHO_MINIMO_SENHA} caracteres.</FormDescription>
              <FormMessage />
            </FormItem>
          )}
        />
        <FormField
          control={form.control}
          name="confirmacao"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Repita a nova senha</FormLabel>
              <FormControl>
                <Input type="password" autoComplete="new-password" {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        <Button type="submit" disabled={form.formState.isSubmitting}>
          Alterar senha
        </Button>
      </form>
    </Form>
  );
}
