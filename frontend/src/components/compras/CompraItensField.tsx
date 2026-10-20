import { useFieldArray, useFormContext } from "react-hook-form";
import { Button } from "@/components/ui/button";
import { FormControl, FormField, FormItem, FormMessage } from "@/components/ui/form";
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
import type { CompraFormValues } from "@/lib/compraSchema";
import type { ProdutoResponse } from "@/types/produto";

const formatoMoeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });

interface CompraItensFieldProps {
  produtos: ProdutoResponse[];
}

export function CompraItensField({ produtos }: CompraItensFieldProps) {
  const form = useFormContext<CompraFormValues>();
  const { fields, append, remove } = useFieldArray({ control: form.control, name: "itens" });
  const itens = form.watch("itens");
  const erroItens = form.formState.errors.itens?.message ?? form.formState.errors.itens?.root?.message;

  return (
    <div className="space-y-3">
      <div className="rounded-lg border border-border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Produto</TableHead>
              <TableHead className="w-28">Quantidade</TableHead>
              <TableHead className="w-32">Custo unitário</TableHead>
              <TableHead className="w-28">Desconto</TableHead>
              <TableHead className="w-28 text-right">Total</TableHead>
              <TableHead className="w-10" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {fields.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} className="py-6 text-center text-muted-foreground">
                  Nenhum item adicionado.
                </TableCell>
              </TableRow>
            ) : (
              fields.map((field, index) => {
                const quantidade = itens?.[index]?.quantidade ?? 0;
                const custoUnitario = itens?.[index]?.custoUnitario ?? 0;
                const desconto = itens?.[index]?.desconto ?? 0;
                const totalItem = quantidade * custoUnitario - desconto;

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
                                  form.setValue(`itens.${index}.custoUnitario`, produto.precoCusto);
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
                        name={`itens.${index}.custoUnitario`}
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
                    <TableCell>
                      <FormField
                        control={form.control}
                        name={`itens.${index}.desconto`}
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
                      {formatoMoeda.format(Number.isFinite(totalItem) ? totalItem : 0)}
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
        onClick={() => append({ produtoId: "", quantidade: 1, custoUnitario: 0, desconto: 0 })}
      >
        Adicionar item
      </Button>
      {erroItens && <p className="text-sm font-medium text-destructive">{erroItens}</p>}
    </div>
  );
}
