import { z } from "zod";

export const lancamentoCaixaSchema = z
  .object({
    tipo: z.enum(["ENTRADA", "SAIDA"]),
    descricao: z.string().trim().min(1, "Descrição é obrigatória"),
    categoriaId: z.string().min(1, "Categoria é obrigatória"),
    valor: z.number({ message: "Valor é obrigatório" }).min(0.01, "Valor deve ser maior que zero"),
    situacao: z.enum(["PREVISTO", "REALIZADO"]),
    dataPrevista: z.string().min(1, "Data prevista é obrigatória"),
    dataEfetiva: z.string(),
    observacoes: z.string(),
  })
  .superRefine((valores, contexto) => {
    if (valores.situacao === "REALIZADO" && !valores.dataEfetiva) {
      contexto.addIssue({ code: "custom", path: ["dataEfetiva"], message: "Data de realização é obrigatória" });
    }
  });

export type LancamentoCaixaFormValues = z.infer<typeof lancamentoCaixaSchema>;
