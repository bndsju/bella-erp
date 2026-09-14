import { z } from "zod";

const hoje = () => new Date().toISOString().slice(0, 10);

const orcamentoItemSchema = z.object({
  produtoId: z.string().min(1, "Produto é obrigatório"),
  quantidade: z.number({ message: "Quantidade é obrigatória" }).min(0.001, "Quantidade deve ser maior que zero"),
  valorUnitario: z.number({ message: "Valor unitário é obrigatório" }).min(0, "Valor unitário não pode ser negativo"),
});

export const orcamentoSchema = z.object({
  clienteId: z.string().min(1, "Cliente é obrigatório"),
  itens: z.array(orcamentoItemSchema).min(1, "Orçamento precisa ter ao menos um item"),
  percentualDesconto: z
    .number({ message: "Percentual de desconto é obrigatório" })
    .min(0, "Percentual de desconto deve estar entre 0 e 100")
    .max(100, "Percentual de desconto deve estar entre 0 e 100"),
  valorFrete: z.number({ message: "Valor do frete é obrigatório" }).min(0, "Valor do frete não pode ser negativo"),
  prazoValidade: z
    .string()
    .min(1, "Prazo de validade é obrigatório")
    .refine((valor) => valor >= hoje(), { message: "Prazo de validade não pode ser uma data passada" }),
  condicaoPagamento: z.string().min(1, "Condição de pagamento é obrigatória"),
  observacoes: z.string().optional(),
});

export type OrcamentoFormValues = z.infer<typeof orcamentoSchema>;
export type OrcamentoItemFormValues = z.infer<typeof orcamentoItemSchema>;
