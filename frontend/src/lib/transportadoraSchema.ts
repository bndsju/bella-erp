import { z } from "zod";

export const transportadoraSchema = z.object({
  nome: z.string().min(1, "Nome é obrigatório"),
  telefone: z.string().optional(),
});

export type TransportadoraFormValues = z.infer<typeof transportadoraSchema>;
