import { z } from "zod";

const pedidoItemSchema = z.object({
  produtoId: z.string().min(1, "Produto é obrigatório"),
  quantidade: z.number({ message: "Quantidade é obrigatória" }).min(0.001, "Quantidade deve ser maior que zero"),
  valorUnitario: z.number({ message: "Valor unitário é obrigatório" }).min(0, "Valor unitário não pode ser negativo"),
});

export const pedidoSchema = z.object({
  clienteId: z.string().min(1, "Cliente é obrigatório"),
  transportadoraId: z.string().optional(),
  itens: z.array(pedidoItemSchema).min(1, "Pedido precisa ter ao menos um item"),
  percentualDesconto: z
    .number({ message: "Percentual de desconto é obrigatório" })
    .min(0, "Percentual de desconto deve estar entre 0 e 100")
    .max(100, "Percentual de desconto deve estar entre 0 e 100"),
  valorFrete: z.number({ message: "Valor do frete é obrigatório" }).min(0, "Valor do frete não pode ser negativo"),
  condicaoPagamento: z.string().min(1, "Condição de pagamento é obrigatória"),
});

export type PedidoFormValues = z.infer<typeof pedidoSchema>;
export type PedidoItemFormValues = z.infer<typeof pedidoItemSchema>;
