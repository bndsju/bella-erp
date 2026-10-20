import { z } from "zod";

export const movimentacaoEstoqueSchema = z.object({
  tipo: z.enum(["ENTRADA", "SAIDA", "AJUSTE_ENTRADA", "AJUSTE_SAIDA"]),
  produtoId: z.string().min(1, "Produto é obrigatório"),
  quantidade: z
    .number({ message: "Quantidade é obrigatória" })
    .min(0.001, "Quantidade deve ser maior que zero"),
  motivo: z.string().min(1, "Motivo é obrigatório"),
  observacao: z.string().optional(),
});

export type MovimentacaoEstoqueFormValues = z.infer<typeof movimentacaoEstoqueSchema>;
