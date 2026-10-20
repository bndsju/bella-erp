import { z } from "zod";

const compraItemSchema = z.object({
  produtoId: z.string().min(1, "Produto é obrigatório"),
  quantidade: z.number({ message: "Quantidade é obrigatória" }).min(0.001, "Quantidade deve ser maior que zero"),
  custoUnitario: z.number({ message: "Custo unitário é obrigatório" }).min(0, "Custo unitário não pode ser negativo"),
  desconto: z.number({ message: "Desconto inválido" }).min(0, "Desconto não pode ser negativo"),
});

export const compraSchema = z
  .object({
    fornecedorId: z.string().min(1, "Fornecedor é obrigatório"),
    previsaoRecebimento: z.string().optional(),
    itens: z.array(compraItemSchema).min(1, "A compra precisa ter ao menos um item"),
    descontoGeral: z.number({ message: "Desconto inválido" }).min(0, "Desconto não pode ser negativo"),
    frete: z.number({ message: "Frete inválido" }).min(0, "Frete não pode ser negativo"),
    outrasDespesas: z.number({ message: "Valor inválido" }).min(0, "Outras despesas não podem ser negativas"),
    condicaoPagamento: z.string().min(1, "Condição de pagamento é obrigatória"),
    observacoes: z.string().optional(),
  })
  .superRefine((valores, contexto) => {
    const vistos = new Set<string>();
    valores.itens.forEach((item, indice) => {
      if (item.produtoId) {
        if (vistos.has(item.produtoId)) {
          contexto.addIssue({
            code: "custom",
            message: "Produto repetido: cada produto deve aparecer uma só vez",
            path: ["itens", indice, "produtoId"],
          });
        }
        vistos.add(item.produtoId);
      }
      if (item.desconto > item.quantidade * item.custoUnitario) {
        contexto.addIssue({
          code: "custom",
          message: "Desconto maior que o valor do item",
          path: ["itens", indice, "desconto"],
        });
      }
    });

    const liquidoItens = valores.itens.reduce(
      (total, item) => total + item.quantidade * item.custoUnitario - item.desconto,
      0,
    );
    if (valores.descontoGeral > liquidoItens) {
      contexto.addIssue({
        code: "custom",
        message: "O desconto não pode ser maior que o valor dos produtos",
        path: ["descontoGeral"],
      });
    }
  });

export type CompraFormValues = z.infer<typeof compraSchema>;
export type CompraItemFormValues = z.infer<typeof compraItemSchema>;
