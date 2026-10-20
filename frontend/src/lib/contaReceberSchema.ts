import { z } from "zod";

const parcelaSchema = z.object({
  valor: z.number({ message: "Valor é obrigatório" }).min(0.01, "Valor deve ser maior que zero"),
  vencimento: z.string().min(1, "Vencimento é obrigatório"),
});

export const contaReceberSchema = z
  .object({
    descricao: z.string().min(1, "Descrição é obrigatória"),
    clienteId: z.string().optional(),
    valorTotal: z.number({ message: "Valor da conta é obrigatório" }).min(0.01, "Valor deve ser maior que zero"),
    modo: z.enum(["UNICO", "PARCELADO"]),
    vencimento: z.string().optional(),
    parcelas: z.array(parcelaSchema),
    observacoes: z.string().optional(),
  })
  .superRefine((valores, contexto) => {
    if (valores.modo === "UNICO") {
      if (!valores.vencimento) {
        contexto.addIssue({ code: "custom", message: "Vencimento é obrigatório", path: ["vencimento"] });
      }
      return;
    }

    if (valores.parcelas.length === 0) {
      contexto.addIssue({ code: "custom", message: "Informe ao menos uma parcela", path: ["parcelas"] });
      return;
    }
    const somaCentavos = valores.parcelas.reduce((total, parcela) => total + Math.round(parcela.valor * 100), 0);
    if (somaCentavos !== Math.round(valores.valorTotal * 100)) {
      contexto.addIssue({
        code: "custom",
        message: "A soma das parcelas deve ser igual ao valor da conta",
        path: ["parcelas"],
      });
    }
  });

export type ContaReceberFormValues = z.infer<typeof contaReceberSchema>;

export function criarRecebimentoSchema(saldoPendente: number) {
  return z
    .object({
      valorRecebido: z.number({ message: "Valor é obrigatório" }).min(0.01, "Valor deve ser maior que zero"),
      dataRecebimento: z.string().min(1, "Data é obrigatória"),
      juros: z.number({ message: "Valor inválido" }).min(0, "Juros não podem ser negativos"),
      multa: z.number({ message: "Valor inválido" }).min(0, "Multa não pode ser negativa"),
      desconto: z.number({ message: "Valor inválido" }).min(0, "Desconto não pode ser negativo"),
      observacao: z.string().optional(),
    })
    .superRefine((valores, contexto) => {
      const abatido = Math.round((valores.valorRecebido + valores.desconto) * 100);
      if (abatido > Math.round(saldoPendente * 100)) {
        contexto.addIssue({
          code: "custom",
          message: "Valor recebido + desconto não pode ultrapassar o saldo da parcela",
          path: ["valorRecebido"],
        });
      }
    });
}

export type RecebimentoFormValues = z.infer<ReturnType<typeof criarRecebimentoSchema>>;
