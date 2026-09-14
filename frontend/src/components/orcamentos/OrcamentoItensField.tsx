import { useFieldArray, useFormContext } from "react-hook-form";
import { Button } from "@/components/ui/button";
import {
  FormControl,
  FormField,
  FormItem,
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
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { OrcamentoFormValues } from "@/lib/orcamentoSchema";
import type { ProdutoResponse } from "@/types/produto";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

interface OrcamentoItensFieldProps {
  produtos: ProdutoResponse[];
}

export function OrcamentoItensField({ produtos }: OrcamentoItensFieldProps) {
  const form = useFormContext<OrcamentoFormValues>();
  const { fields, append, remove } = useFieldArray({ control: form.control, name: "itens" });
  const itens = form.watch("itens");
  const erroItens = form.formState.errors.itens?.message;

  return (
    <div className="space-y-3">
      <div className="rounded-lg border border-border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Produto</TableHead>
              <TableHead className="w-28">Quantidade</TableHead>
              <TableHead className="w-32">Valor unitário</TableHead>
              <TableHead className="w-28 text-right">Subtotal</TableHead>
              <TableHead className="w-10" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {fields.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="py-6 text-center text-muted-foreground">
                  Nenhum item adicionado.
                </TableCell>
              </TableRow>
            ) : (
              fields.map((field, index) => {
                const quantidade = itens?.[index]?.quantidade ?? 0;
                const valorUnitario = itens?.[index]?.valorUnitario ?? 0;
                const subtotalItem = quantidade * valorUnitario;

                return (
                  <TableRow key={field.id}>
                    <TableCell>
                      <FormField
                        control={form.control}
                        name={`itens.${index}.produtoId`}
                        render={({ field: campo }) => (
                          <FormItem>
                            <Select
                              value={campo.value}
                              onValueChange={(valor) => {
                                campo.onChange(valor);
                                const produto = produtos.find((p) => p.id === valor);
                                if (produto) {
                                  form.setValue(`itens.${index}.valorUnitario`, produto.precoVenda);
                                }
                              }}
                            >
                              <FormControl>
                                <SelectTrigger className="w-full">
                                  <SelectValue placeholder="Selecione o produto" />
                                </SelectTrigger>
                              </FormControl>
                              <SelectContent>
                                {produtos.map((produto) => (
                                  <SelectItem key={produto.id} value={produto.id}>
                                    {produto.codigoInterno} — {produto.nome}
                                  </SelectItem>
                                ))}
                              </SelectContent>
                            </Select>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </TableCell>
                    <TableCell>
                      <FormField
                        control={form.control}
                        name={`itens.${index}.quantidade`}
                        render={({ field: campo }) => (
                          <FormItem>
                            <FormControl>
                              <Input
                                type="number"
                                step="0.001"
                                min="0"
                                {...campo}
                                onChange={(e) => campo.onChange(e.target.valueAsNumber)}
                              />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </TableCell>
                    <TableCell>
                      <FormField
                        control={form.control}
                        name={`itens.${index}.valorUnitario`}
                        render={({ field: campo }) => (
                          <FormItem>
                            <FormControl>
                              <Input
                                type="number"
                                step="0.01"
                                min="0"
                                {...campo}
                                onChange={(e) => campo.onChange(e.target.valueAsNumber)}
                              />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                    </TableCell>
                    <TableCell className="text-right text-sm">
                      {formatoMoeda.format(Number.isFinite(subtotalItem) ? subtotalItem : 0)}
                    </TableCell>
                    <TableCell>
                      <Button type="button" variant="ghost" size="sm" onClick={() => remove(index)}>
                        Remover
                      </Button>
                    </TableCell>
                  </TableRow>
                );
              })
            )}
          </TableBody>
        </Table>
      </div>
      <Button
        type="button"
        variant="outline"
        size="sm"
        onClick={() => append({ produtoId: "", quantidade: 1, valorUnitario: 0 })}
      >
        Adicionar item
      </Button>
      {erroItens && <p className="text-sm font-medium text-destructive">{erroItens}</p>}
    </div>
  );
}
