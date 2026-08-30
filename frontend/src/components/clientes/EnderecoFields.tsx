import { useState } from "react";
import { useFormContext } from "react-hook-form";
import { toast } from "sonner";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { formatarCep } from "@/lib/masks";
import { buscarEnderecoPorCep, ViaCepError } from "@/lib/viaCep";
import { UFS } from "@/lib/uf";
import type { ClienteFormValues } from "@/lib/clienteSchema";

export function EnderecoFields() {
  const form = useFormContext<ClienteFormValues>();
  const [buscandoCep, setBuscandoCep] = useState(false);

  async function preencherPorCep(cep: string) {
    setBuscandoCep(true);
    try {
      const endereco = await buscarEnderecoPorCep(cep);
      if (!endereco) {
        toast.error("CEP não encontrado");
        return;
      }
      form.setValue("endereco.logradouro", endereco.logradouro ?? "");
      form.setValue("endereco.complemento", endereco.complemento ?? "");
      form.setValue("endereco.bairro", endereco.bairro ?? "");
      form.setValue("endereco.cidade", endereco.cidade ?? "");
      form.setValue("endereco.uf", endereco.uf ?? "");
    } catch (erro) {
      if (erro instanceof ViaCepError) {
        toast.error(erro.message);
      } else {
        toast.error("Não foi possível buscar o CEP agora");
      }
    } finally {
      setBuscandoCep(false);
    }
  }

  return (
    <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
      <FormField
        control={form.control}
        name="endereco.cep"
        render={({ field }) => (
          <FormItem className="col-span-2 sm:col-span-1">
            <FormLabel>CEP</FormLabel>
            <FormControl>
              <Input
                {...field}
                placeholder="00000-000"
                disabled={buscandoCep}
                onChange={(e) => field.onChange(formatarCep(e.target.value))}
                onBlur={(e) => {
                  field.onBlur();
                  if (e.target.value.replace(/\D/g, "").length === 8) {
                    void preencherPorCep(e.target.value);
                  }
                }}
              />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />

      <FormField
        control={form.control}
        name="endereco.logradouro"
        render={({ field }) => (
          <FormItem className="col-span-2 sm:col-span-3">
            <FormLabel>Logradouro</FormLabel>
            <FormControl>
              <Input placeholder="Rua, avenida..." {...field} />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />

      <FormField
        control={form.control}
        name="endereco.numero"
        render={({ field }) => (
          <FormItem>
            <FormLabel>Número</FormLabel>
            <FormControl>
              <Input placeholder="123" {...field} />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />

      <FormField
        control={form.control}
        name="endereco.complemento"
        render={({ field }) => (
          <FormItem className="col-span-2 sm:col-span-1">
            <FormLabel>Complemento</FormLabel>
            <FormControl>
              <Input placeholder="Apto, sala..." {...field} />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />

      <FormField
        control={form.control}
        name="endereco.bairro"
        render={({ field }) => (
          <FormItem className="col-span-2 sm:col-span-1">
            <FormLabel>Bairro</FormLabel>
            <FormControl>
              <Input {...field} />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />

      <FormField
        control={form.control}
        name="endereco.cidade"
        render={({ field }) => (
          <FormItem className="col-span-2 sm:col-span-2">
            <FormLabel>Cidade</FormLabel>
            <FormControl>
              <Input {...field} />
            </FormControl>
            <FormMessage />
          </FormItem>
        )}
      />

      <FormField
        control={form.control}
        name="endereco.uf"
        render={({ field }) => (
          <FormItem>
            <FormLabel>UF</FormLabel>
            <Select value={field.value} onValueChange={field.onChange}>
              <FormControl>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="UF" />
                </SelectTrigger>
              </FormControl>
              <SelectContent>
                {UFS.map((uf) => (
                  <SelectItem key={uf} value={uf}>
                    {uf}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <FormMessage />
          </FormItem>
        )}
      />
    </div>
  );
}
